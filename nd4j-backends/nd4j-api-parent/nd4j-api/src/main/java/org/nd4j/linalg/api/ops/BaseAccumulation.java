/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 *
 */

package org.nd4j.linalg.api.ops;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import onnx.OnnxProto3;
import org.apache.commons.lang3.ArrayUtils;
import org.nd4j.autodiff.functions.DifferentialFunction;
import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.imports.graphmapper.onnx.OnnxGraphMapper;
import org.nd4j.imports.graphmapper.tf.TFGraphMapper;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.tensorflow.framework.AttrValue;
import org.tensorflow.framework.GraphDef;
import org.tensorflow.framework.NodeDef;

import java.util.Map;

/**
 * Base class for accumulation, initiates the initial entry
 * with respect to the child class. Also contains baseline fields
 * for the over all field with accumulation.
 *
 * @author Adam Gibson
 */
@Slf4j
public abstract class BaseAccumulation extends BaseOp implements Accumulation {
    protected Number finalResult;

    protected boolean keepDims;

    public BaseAccumulation(SameDiff sameDiff,
                            DifferentialFunction i_v,
                            int[] dimensions,boolean keepDims) {
        super(sameDiff,new Object[]{dimensions});
        if (i_v != null) {
            sameDiff.associateFunctionsAsArgs(new DifferentialFunction[] {i_v},this);
            this.dimensions = dimensions;
            sameDiff.putShapeForVertexId(vertexId,Shape.getReducedShape(i_v.getResultShape(),dimensions));
            f().validateDifferentialFunctionsameDiff(i_v);
            addAsNewVertexId();
            this.keepDims = keepDims;
            f().addFunctionEdges(this);

        } else {
            throw new IllegalArgumentException("Input not null variable.");
        }

    }

    public BaseAccumulation(SameDiff sameDiff,
                            DifferentialFunction i_v,
                            DifferentialFunction i_v2,
                            int[] dimensions,boolean keepDims) {
        super(sameDiff,new Object[]{dimensions});
        if (i_v != null) {
            sameDiff.associateFunctionsAsArgs(new DifferentialFunction[] {i_v,i_v2},this);
            this.dimensions = dimensions;
            sameDiff.putShapeForVertexId(vertexId,Shape.getReducedShape(i_v.getResultShape(),dimensions));
            f().validateDifferentialFunctionsameDiff(i_v);
            f().validateDifferentialFunctionsameDiff(i_v2);
            addAsNewVertexId();
            this.keepDims = keepDims;
            f().addFunctionEdges(this);


        } else {
            throw new IllegalArgumentException("Input not null variable.");
        }

    }



    public BaseAccumulation(SameDiff sameDiff,
                            DifferentialFunction i_v,
                            int[] dimensions) {
        this(sameDiff,i_v,dimensions,false);

    }

    public BaseAccumulation(SameDiff sameDiff,
                            DifferentialFunction i_v,
                            DifferentialFunction i_v2,
                            int[] dimensions) {
        this(sameDiff,i_v,i_v2,dimensions,false);
    }



    public BaseAccumulation() {}




    /**
     * Initialize with the given
     * input, pairwise transform, result, and number
     * of elements
     *
     * @param x the input
     * @param y the pairwise transform
     * @param z the result
     * @param n the number of elements
     */
    public BaseAccumulation(INDArray x, INDArray y, INDArray z, long n) {
        super(x, y, z, n);
        init();
        //      if (y != null)
        //            LinAlgExceptions.assertSameLength(x, y);
        //LinAlgExceptions.assertSameLength(x, z);

    }

    public BaseAccumulation(INDArray x, INDArray y, long n) {
        this(x, y, x, n);
    }

    public BaseAccumulation(INDArray x) {
        this(x, null, x, x.lengthLong());
    }

    public BaseAccumulation(INDArray x, INDArray y) {
        this(x, y, x, x.lengthLong());
        //if (y != null)
        //    LinAlgExceptions.assertSameLength(x, y);
    }

    public BaseAccumulation(SameDiff sameDiff) {
        this.sameDiff = sameDiff;
    }




    private void init() {
        if (z == null || x == z)
            init(x, y, x, x.lengthLong());
        else
            init(x, y, z, x.lengthLong());
    }

    @Override
    public INDArray noOp() {
        if (z != null && x != z)
            return z().assign(x);
        else
            return x().dup(x().ordering());
    }



    @Override
    public void initFromTensorFlow(NodeDef nodeDef, SameDiff initWith, Map<String, AttrValue> attributesForNode, GraphDef graph) {
        if (!attributesForNode.containsKey("axis") && !hasReductionIndices(nodeDef)) {
            this.dimensions = new int[] { Integer.MAX_VALUE };
        }
        else if(hasReductionIndices(nodeDef)) {
            NodeDef reductionNode = null;
            for(int i = 0; i < graph.getNodeCount(); i++) {
                if (graph.getNode(i).getName().equals(nodeDef.getName() + "/reduction_indices")) {
                    reductionNode = graph.getNode(i);
                    val arr = TFGraphMapper.getInstance().getNDArrayFromTensor("value", graph.getNode(i), graph);

                    boolean keepAxis = nodeDef.getAttrOrThrow("keep_dims").getB();

                    // keepAxis = false by default
                    int[] dimensions = ArrayUtils.add(arr.data().asInt(), 0, keepAxis ? 1 : 0);


                    this.dimensions = dimensions;
                    break;
                }
            }

            if(reductionNode == null)
                throw new ND4JIllegalStateException("No node found!");



        }
        else {
            val dims = TFGraphMapper.getInstance().getNDArrayFromTensor("axis",nodeDef,graph).data().asInt();
            this.dimensions = dims;
        }

        if(attributesForNode.containsKey("keep_dims")) {
            val keepDims = attributesForNode.get("keep_dims").getB();
            this.keepDims = keepDims;
        }
    }

    protected boolean hasReductionIndices(NodeDef nodeDef) {
        for(int i = 0; i < nodeDef.getInputCount(); i++) {
            if(nodeDef.getInput(i).contains("reduction_indices")) {
                return true;
            }
        }

        return false;
    }


    @Override
    public void initFromOnnx(OnnxProto3.NodeProto node, SameDiff initWith, Map<String, OnnxProto3.AttributeProto> attributesForNode, OnnxProto3.GraphProto graph) {
        if (!attributesForNode.containsKey("axes")) {
            this.dimensions = new int[] { Integer.MAX_VALUE };
        }
        else {
            val map = OnnxGraphMapper.getInstance().getAttrMap(node);
            val dims = Ints.toArray(map.get("axes").getIntsList());
            this.dimensions = dims;
        }
    }


    @Override
    public void initWithArrays(Map<String, INDArray> arrayMap) {
        super.initWithArrays(arrayMap);
    }

    @Override
    public void setFinalResult(double value) {
        this.finalResult = value;
    }

    @Override
    public Number getFinalResult() {
        return finalResult;
    }

    @Override
    public double zeroDouble() {
        return 0;
    }

    @Override
    public float zeroFloat() {
        return 0;
    }

    @Override
    public float zeroHalf() {
        return 0;
    }

    @Override
    public Type opType() {
        return Type.REDUCE;
    }
}
