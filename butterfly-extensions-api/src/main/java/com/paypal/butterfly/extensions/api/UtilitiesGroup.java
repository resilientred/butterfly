package com.paypal.butterfly.extensions.api;

import com.paypal.butterfly.extensions.api.exception.TransformationDefinitionException;
import com.paypal.butterfly.extensions.api.utilities.Log;
import org.slf4j.event.Level;

import java.io.File;
import java.util.*;

/**
 *
 * @author facarvalho
 */
public class UtilitiesGroup extends TransformationUtility<UtilitiesGroup> implements TransformationUtilityList {

    private List<TransformationUtility> utilityList = new ArrayList<TransformationUtility>();

    private Set<String> utilityNames = new HashSet<>();

    private static final String DESCRIPTION = "Transformation utility group";

    public UtilitiesGroup() {
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String add(TransformationUtility utility) {
        if (getParent() == null) {
            String exceptionMessage = String.format("Invalid attempt to add transformation utility to utilities group. This group has to be added to a transformation utilities parent first. Add it to another group, or to a transformation template.");
            throw new  TransformationDefinitionException(exceptionMessage);
        }
        if (utility.getParent() != null) {
            String exceptionMessage = String.format("Invalid attempt to add already registered transformation utility %s to transformation utility group %s", utility.getName(), getName());
            throw new  TransformationDefinitionException(exceptionMessage);
        }
        // TODO
        // Here I am checking for name collisions only in this group, but that is isolated from TUs added to the parent, and vice-versa
        if (utility.getName() != null && utilityNames.contains(utility.getName())) {
            String exceptionMessage = String.format("Invalid attempt to add transformation utility %s to utilities group %s. Its name is already registered", utility.getName(), getName());
            throw new  TransformationDefinitionException(exceptionMessage);
        }
        if (!utility.isFileSet()) {
            String exceptionMessage = String.format("Neither absolute, nor relative path, have been set for transformation utility %s", utility.getName());
            throw new  TransformationDefinitionException(exceptionMessage);
        }

        int order;
        synchronized (this) {
            utilityList.add(utility);

            // This is the order of execution of this utility
            // Not to be confused with the index of the element in the list,
            // Since the first utility will be assigned order 1 (not 0)
            order = utilityList.size();
        }

        utility.setParent(this, order);

        utilityNames.add(utility.getName());

        return utility.getName();
    }

    @Override
    public String add(TransformationUtility utility, String utilityName) {
        utility.setName(utilityName);
        return add(utility);
    }

    @Override
    public String addMultiple(TransformationOperation templateOperation, String... attributes) {
        return add(new MultipleOperations(templateOperation).setFiles(attributes));
    }

    @Override
    public final void log(String logMessage) {
        add(new Log().setLogMessage(logMessage));
    }

    @Override
    public final void log(Level logLevel, String logMessage) {
        add(new Log().setLogLevel(logLevel).setLogMessage(logMessage));
    }

    @Override
    public final void log(String logMessage, String... attributeNames) {
        add(new Log().setLogMessage(logMessage).setAttributeNames(attributeNames));
    }

    @Override
    public final void log(Level logLevel, String logMessage, String... attributeNames) {
        add(new Log().setLogLevel(logLevel).setLogMessage(logMessage).setAttributeNames(attributeNames));
    }

    @Override
    public List<TransformationUtility> getUtilities() {
        return getChildren();
    }

    @Override
    public List<TransformationUtility> getChildren() {
        return Collections.unmodifiableList(utilityList);
    }

    @Override
    protected ExecutionResult execution(File transformedAppFolder, TransformationContext transformationContext) {
// FIXME this won't work because clone doesn't propagate many essential attributes, such as the file, order, parent, etc
//        List<TransformationUtility> utilityClonesList = new ArrayList<>();
//
//        try {
//            for (TransformationUtility utility : utilityList) {
//                utilityClonesList.add(utility.clone());
//            }
//        } catch (CloneNotSupportedException e) {
//            TransformationUtilityException tue = new TransformationUtilityException("Transformation utility is not cloneable", e);
//            return TUExecutionResult.error(this, tue);
//        }
//
//        TUExecutionResult result = TUExecutionResult.value(this, utilityClonesList);
        TUExecutionResult result = TUExecutionResult.value(this, getChildren());
        return result;
    }

}
