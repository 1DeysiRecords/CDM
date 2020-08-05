// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for license information.

package com.microsoft.commondatamodel.objectmodel.cdm.projections;

import com.microsoft.commondatamodel.objectmodel.cdm.CdmAttributeContext;
import com.microsoft.commondatamodel.objectmodel.cdm.CdmCorpusContext;
import com.microsoft.commondatamodel.objectmodel.cdm.CdmEntityReference;
import com.microsoft.commondatamodel.objectmodel.cdm.CdmObject;
import com.microsoft.commondatamodel.objectmodel.cdm.CdmObjectBase;
import com.microsoft.commondatamodel.objectmodel.cdm.CdmObjectDefinitionBase;
import com.microsoft.commondatamodel.objectmodel.enums.CdmAttributeContextType;
import com.microsoft.commondatamodel.objectmodel.enums.CdmObjectType;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.ResolvedAttributeSet;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.ResolvedTraitSet;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.expressionparser.ExpressionTree;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.expressionparser.InputValues;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.expressionparser.Node;
import com.microsoft.commondatamodel.objectmodel.resolvedmodel.projections.*;
import com.microsoft.commondatamodel.objectmodel.utilities.*;
import com.microsoft.commondatamodel.objectmodel.utilities.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class for Projection
 */
public class CdmProjection extends CdmObjectDefinitionBase {
    private String TAG = CdmProjection.class.getSimpleName();
    private String condition;
    private Node conditionExpressionTreeRoot;
    private CdmOperationCollection operations;
    private CdmEntityReference source;

    /**
     * Projection constructor
     */
    public CdmProjection(final CdmCorpusContext ctx) {
        super(ctx);
        this.setObjectType(CdmObjectType.ProjectionDef);
        this.operations = new CdmOperationCollection(ctx, this);
    }

    /**
     * Property of a projection that holds the condition expression string
     */
    public String getCondition() {
        return condition;
    }

    public void setCondition(final String condition) {
        this.condition = condition;
    }

    /**
     * Condition expression tree that is built out of a condition expression string
     *
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Deprecated
    public Node getConditionExpressionTreeRoot() {
        return conditionExpressionTreeRoot;
    }

    /**
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Deprecated
    public void setConditionExpressionTreeRoot(final Node conditionExpressionTreeRoot) {
        this.conditionExpressionTreeRoot = conditionExpressionTreeRoot;
    }

    /**
     * Property of a projection that holds a collection of operations
     */
    public CdmOperationCollection getOperations() {
        return operations;
    }

    /**
     * Property of a projection that holds the source of the operation
     */
    public CdmEntityReference getSource() {
        return source;
    }

    public void setSource(final CdmEntityReference source) {
        this.source = source;
    }

    @Override
    public CdmObject copy(ResolveOptions resOpt, CdmObject host) {
        Logger.error(TAG, this.getCtx(), "Projection operation not implemented yet.", "copy");
        return new CdmProjection(this.getCtx());
    }

    /**
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Override
    @Deprecated
    public Object copyData(final ResolveOptions resOpt, final CopyOptions options) {
        return CdmObjectBase.copyData(this, resOpt, options, CdmProjection.class);
    }

    @Override
    public String getName() {
        return "projection";
    }

    /**
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Override
    @Deprecated
    public CdmObjectType getObjectType() {
        return CdmObjectType.ProjectionDef;
    }

    @Override
    public boolean isDerivedFrom(final String baseDef, ResolveOptions resOpt) {
        // Since projections don't support inheritance, return false
        return false;
    }

    @Override
    public boolean validate() {
        ArrayList<String> missingFields = new ArrayList<>();

        if (this.source == null) {
            missingFields.add("source");
        }
        if (missingFields.size() > 0) {
            Logger.error(TAG, this.getCtx(), Errors.validateErrorString(this.getAtCorpusPath(), missingFields));
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(final String pathFrom, final VisitCallback preChildren, final VisitCallback postChildren) {
        String path = "";
        if (!this.getCtx().getCorpus().getBlockDeclaredPathChanges()) {
            path = this.getDeclaredPath();
            if (StringUtils.isNullOrTrimEmpty(path)) {
                path = pathFrom + "projection";
                this.setDeclaredPath(path);
            }
        }

        if (preChildren != null && preChildren.invoke(this, path)) {
            return false;
        }

        if (this.source != null) {
            if (this.source.visit(path + "/source/", preChildren, postChildren)) {
                return true;
            }
        }

        boolean result = false;
        if (this.operations != null && this.operations.size() > 0) {
            // since this.operations.VisitList results is non-unique attribute context paths if there are 2 or more operations of the same type.
            // e.g. with composite keys
            // the solution is to add a unique identifier to the path by adding the operation index or opIdx
            for (int opIndex = 0; opIndex < this.operations.size(); opIndex++) {
                this.operations.get(opIndex).setIndex(opIndex + 1);
                if ((this.operations.getAllItems().get(opIndex) != null) &&
                        (this.operations.getAllItems().get(opIndex).visit(path + "/operation/index" + (opIndex + 1) + "/", preChildren, postChildren))) {
                    result = true;
                } else {
                    result = false;
                }
            }
            if (result)
                return true;
        }

        if (postChildren != null && postChildren.invoke(this, path)) {
            return true;
        }

        return false;
    }

    /**
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Override
    @Deprecated
    public ResolvedTraitSet fetchResolvedTraits() {
        return this.source.fetchResolvedTraits(null);
    }

    /**
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Override
    @Deprecated
    public ResolvedTraitSet fetchResolvedTraits(ResolveOptions resOpt) {
        return this.source.fetchResolvedTraits(resOpt);
    }

    /**
     * A function to construct projection context and populate the resolved attribute set that ExtractResolvedAttributes method can then extract
     * This function is the entry point for projection resolution.
     * This function is expected to do the following 3 things:
     * - Create an condition expression tree & default if appropriate
     * - Create and initialize Projection Context
     * - Process operations
     *
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Deprecated
    public ProjectionContext constructProjectionContext(ProjectionDirective projDirective, CdmAttributeContext attrCtx) {
        ProjectionContext projContext = null;

        if (StringUtils.isNullOrTrimEmpty(this.condition)) {
            // if no condition is provided, get default condition and persist
            this.condition = ConditionExpression.getDefaultConditionExpression(this.operations, this.getOwner());
        }
        // create an expression tree based on the condition
        ExpressionTree tree = new ExpressionTree();
        this.conditionExpressionTreeRoot = tree.constructExpressionTree(this.condition);
        if (this.conditionExpressionTreeRoot == null) {
            Logger.info(TAG, this.getCtx(), "Optional expression missing. Implicit expression will automatically apply.", "constructProjectionContext");
        }

        if (attrCtx != null) {
            // Add projection to context tree
            AttributeContextParameters acpProj = new AttributeContextParameters();
            acpProj.setUnder(attrCtx);
            acpProj.setType(CdmAttributeContextType.Projection);
            acpProj.setName(this.fetchObjectDefinitionName());
            acpProj.setRegarding(projDirective.getOwnerRef());
            acpProj.setIncludeTraits(false);
            CdmAttributeContext acProj = CdmAttributeContext.createChildUnder(projDirective.getResOpt(), acpProj);

            AttributeContextParameters acpSource = new AttributeContextParameters();
            acpSource.setUnder(acProj);
            acpSource.setType(CdmAttributeContextType.Source);
            acpSource.setName("source");
            acpSource.setRegarding(null);
            acpSource.setIncludeTraits(false);
            CdmAttributeContext acSource = CdmAttributeContext.createChildUnder(projDirective.getResOpt(), acpSource);

            if (this.source.fetchObjectDefinition(projDirective.getResOpt()).getObjectType() == CdmObjectType.ProjectionDef) {
                // A Projection

                projContext = ((CdmProjection)this.source.getExplicitReference()).constructProjectionContext(projDirective, acSource);
            } else {
                // An Entity Reference

                AttributeContextParameters acpSourceProjection = new AttributeContextParameters();
                acpSourceProjection.setUnder(acSource);
                acpSourceProjection.setType(CdmAttributeContextType.Entity);
                acpSourceProjection.setName(this.source.getNamedReference() != null ? this.source.getNamedReference() : this.source.getExplicitReference().getName());
                acpSourceProjection.setRegarding(this.source);
                acpSourceProjection.setIncludeTraits(false);
                ResolvedAttributeSet ras = this.source.fetchResolvedAttributes(projDirective.getResOpt(), acpSourceProjection);

                // Initialize the projection context

                CdmCorpusContext ctx = (projDirective.getOwner() != null ? projDirective.getOwner().getCtx() : null);

                ProjectionAttributeStateSet pasSet = null;

                // if polymorphic keep original source as previous state
                Map<String, List<ProjectionAttributeState>> polySourceSet = null;
                if (projDirective.getIsSourcePolymorphic()) {
                    polySourceSet = ProjectionResolutionCommonUtil.getPolymorphicSourceSet(projDirective, ctx, this.source, acpSourceProjection);
                }

                // now initialize projection attribute state
                pasSet = ProjectionResolutionCommonUtil.initializeProjectionAttributeStateSet(projDirective, ctx, ras, projDirective.getIsSourcePolymorphic(), polySourceSet);

                projContext = new ProjectionContext(projDirective, ras.getAttributeContext());
                projContext.setCurrentAttributeStateSet(pasSet);
            }

            boolean isConditionValid = false;
            if (this.conditionExpressionTreeRoot != null) {
                InputValues input = new InputValues();
                input.setNoMaxDepth(projDirective.getHasNoMaximumDepth());
                input.setIsArray(projDirective.getIsArray());
                input.setReferenceOnly(projDirective.getIsReferenceOnly());
                input.setNormalized(projDirective.getIsNormalized());
                input.setStructured(projDirective.getIsStructured());

                int currentDepth = projDirective.getCurrentDepth();
                input.setNextDepth(++currentDepth);
                projDirective.setCurrentDepth(currentDepth);

                input.setMaxDepth(projDirective.getMaximumDepth());
                input.setMinCardinality(projDirective.getCardinality() != null ? projDirective.getCardinality().getMinimumNumber() : null);
                input.setMaxCardinality(projDirective.getCardinality() != null ? projDirective.getCardinality().getMaximumNumber() : null);

                isConditionValid = (boolean) ExpressionTree.evaluateExpressionTree(this.conditionExpressionTreeRoot, input);
            }

            if (isConditionValid && this.operations != null && this.operations.size() > 0) {
                // Just in case new operations were added programmatically, reindex operations
                for (int i = 0; i < this.operations.size(); i++) {
                    this.operations.get(i).setIndex(i + 1);
                }

                // Operation

                AttributeContextParameters acpGenAttrSet = new AttributeContextParameters();
                acpGenAttrSet.setUnder(attrCtx);
                acpGenAttrSet.setType(CdmAttributeContextType.GeneratedSet);
                acpGenAttrSet.setName("_generatedAttributeSet");
                CdmAttributeContext acGenAttrSet = CdmAttributeContext.createChildUnder(projDirective.getResOpt(), acpGenAttrSet);

                AttributeContextParameters acpGenAttrRound0 = new AttributeContextParameters();
                acpGenAttrRound0.setUnder(acGenAttrSet);
                acpGenAttrRound0.setType(CdmAttributeContextType.GeneratedRound);
                acpGenAttrRound0.setName("_generatedAttributeRound0");
                CdmAttributeContext acGenAttrRound0 = CdmAttributeContext.createChildUnder(projDirective.getResOpt(), acpGenAttrRound0);

                // Start with an empty list for each projection
                ProjectionAttributeStateSet pasOperations = new ProjectionAttributeStateSet(projContext.getCurrentAttributeStateSet().getCtx());
                for (CdmOperationBase operation : this.operations) {
                    // Evaluate projections and apply to empty state
                    ProjectionAttributeStateSet newPasOperations = operation.appendProjectionAttributeState(projContext, pasOperations, acGenAttrRound0);

                    // If the operations fails or it is not implemented the projection cannot be evaluated so keep previous valid state.
                    if (newPasOperations != null)
                    {
                        pasOperations = newPasOperations;
                    }
                }

                // Finally update the current state to the projection context
                projContext.setCurrentAttributeStateSet(pasOperations);
            } else {
                // Pass Through - no operations to process
            }
        }

        return projContext;
    }

    /**
     * Create resolved attribute set based on the CurrentResolvedAttribute array
     *
     * @deprecated This function is extremely likely to be removed in the public interface, and not
     * meant to be called externally at all. Please refrain from using it.
     */
    @Deprecated
    public ResolvedAttributeSet extractResolvedAttributes(ProjectionContext projCtx) {
        ResolvedAttributeSet resolvedAttributeSet = new ResolvedAttributeSet();
        resolvedAttributeSet.setAttributeContext(projCtx.getCurrentAttributeContext());

        for (ProjectionAttributeState pas : projCtx.getCurrentAttributeStateSet().getValues()) {
            resolvedAttributeSet.merge(pas.getCurrentResolvedAttribute(), pas.getCurrentResolvedAttribute().getAttCtx());
        }

        return resolvedAttributeSet;
    }
}
