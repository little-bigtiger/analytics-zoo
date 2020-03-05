/*
 * Copyright 2018 Analytics Zoo Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.analytics.zoo.serving.utils

import com.intel.analytics.zoo.pipeline.inference.InferenceModel
import org.apache.spark.sql.DataFrame

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object AsyncUtils {
  def writeServingSummay(model: InferenceModel,
                         df: DataFrame,
                         start: Long,
                         end: Long,
                         timeStamp: Int,
                         lastCnt: Int): Future[(Int, Int)] = Future{
    val microBatchSize = df.count()
    val microBatchLatency = (end - start) / 1e9
    val microBatchThroughPut = (microBatchSize / microBatchLatency).toFloat
    println(s"Inferece end. Input size $microBatchSize. " +
      s"Latency $microBatchLatency, Throughput $microBatchThroughPut")

    val totalCnt = lastCnt + microBatchSize.toInt
    (timeStamp until timeStamp + microBatchLatency.toInt).foreach( time => {
      model.inferenceSummary.addScalar(
        "Serving Throughput", microBatchThroughPut, time)
      model.inferenceSummary.addScalar(
        "Total Records Number", totalCnt, time)
    })
    (timeStamp, totalCnt)
  }

}
