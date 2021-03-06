/*
 * Copyright 2016, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hive.quantiles;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.parse.SemanticException;
import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFParameterInfo;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector.PrimitiveCategory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;

@Description(name = "DataToSketch", value = "_FUNC_(value, k) - "
  + "Returns a QuantilesSketch in a serialized form as a binary blob."
  + " Values must be of type double."
  + " Parameter k controls the accuracy and the size of the sketch."
  + " If k is ommitted, the default is used.")
public class DataToDoublesSketchUDAF extends AbstractGenericUDAFResolver {

  @Override
  public GenericUDAFEvaluator getEvaluator(final GenericUDAFParameterInfo info) 
      throws SemanticException {
    final ObjectInspector[] inspectors = info.getParameterObjectInspectors();
    if (inspectors.length != 1 && inspectors.length != 2) {
      throw new UDFArgumentException("One or two arguments expected");
    }
    ObjectInspectorValidator.validateGivenPrimitiveCategory(inspectors[0], 0, 
        PrimitiveCategory.DOUBLE);
    if (inspectors.length == 2) {
      ObjectInspectorValidator.validateGivenPrimitiveCategory(inspectors[1], 1, 
          PrimitiveCategory.INT);
    }
    return new DataToSketchEvaluator();
  }

  static class DataToSketchEvaluator extends DoublesEvaluator {

    @SuppressWarnings("deprecation")
    @Override
    public void iterate(final AggregationBuffer buf, final Object[] data) throws HiveException {
      if (data[0] == null) return;
      final DoublesUnionState state = (DoublesUnionState) buf;
      if (!state.isInitialized() && kObjectInspector != null) {
        final int k = PrimitiveObjectInspectorUtils.getInt(data[1], kObjectInspector);
        state.init(k);
      }
      final double value = (double) inputObjectInspector.getPrimitiveJavaObject(data[0]);
      state.update(value);
    }

  }

}
