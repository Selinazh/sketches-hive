/*
 * Copyright 2016, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.sketches.hive.tuple;

import org.apache.hadoop.hive.serde2.objectinspector.PrimitiveObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorUtils;

import com.yahoo.sketches.tuple.ArrayOfDoublesSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketch;
import com.yahoo.sketches.tuple.ArrayOfDoublesUpdatableSketchBuilder;

class ArrayOfDoublesSketchState extends ArrayOfDoublesState {

  private ArrayOfDoublesUpdatableSketch sketch_;

  boolean isInitialized() {
    return sketch_ != null;
  }

  void init(final int nominalNumEntries, final float samplingProbability, final int numValues) {
    super.init(nominalNumEntries, numValues);
    sketch_ = new ArrayOfDoublesUpdatableSketchBuilder().setNominalEntries(nominalNumEntries)
        .setSamplingProbability(samplingProbability).setNumberOfValues(numValues).build();
  }

  void update(final Object[] data, final PrimitiveObjectInspector keyInspector, final PrimitiveObjectInspector[] valuesInspectors) {
    final double[] values = new double[valuesInspectors.length];
    for (int i = 0; i < values.length; i++) {
      values[i] = PrimitiveObjectInspectorUtils.getDouble(data[i + 1], valuesInspectors[i]);
    }
    switch (keyInspector.getPrimitiveCategory()) {
    case BINARY:
      sketch_.update(PrimitiveObjectInspectorUtils.getBinary(data[0], keyInspector).getBytes(), values);
      return;
    case BYTE:
      sketch_.update(PrimitiveObjectInspectorUtils.getByte(data[0], keyInspector), values);
      return;
    case DOUBLE:
      sketch_.update(PrimitiveObjectInspectorUtils.getDouble(data[0], keyInspector), values);
      return;
    case FLOAT:
      sketch_.update(PrimitiveObjectInspectorUtils.getFloat(data[0], keyInspector), values);
      return;
    case INT:
      sketch_.update(PrimitiveObjectInspectorUtils.getInt(data[0], keyInspector), values);
      return;
    case LONG:
      sketch_.update(PrimitiveObjectInspectorUtils.getLong(data[0], keyInspector), values);
      return;
    case STRING:
      sketch_.update(PrimitiveObjectInspectorUtils.getString(data[0], keyInspector), values);
      return;
    default:
      throw new IllegalArgumentException(
          "Unrecongnized input data type, please use data of type binary, byte, double, float, int, long, or string only.");
    }
  }

  @Override
  ArrayOfDoublesSketch getResult() {
    if (sketch_ == null) return null;
    // assumes that it is called once at the end of processing since trimming to nominal number of entries is expensive
    sketch_.trim();
    return sketch_.compact();
  }

  @Override
  void reset() {
    sketch_ = null;    
  }

}
