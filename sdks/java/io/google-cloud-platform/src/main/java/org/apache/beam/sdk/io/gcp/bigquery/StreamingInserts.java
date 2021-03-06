/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.sdk.io.gcp.bigquery;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;

/**
 * PTransform that performs streaming BigQuery write. To increase consistency, it leverages
 * BigQuery's best effort de-dup mechanism.
 */
@SuppressWarnings({
  "nullness" // TODO(https://issues.apache.org/jira/browse/BEAM-10402)
})
public class StreamingInserts<DestinationT, ElementT>
    extends PTransform<PCollection<KV<DestinationT, ElementT>>, WriteResult> {
  private BigQueryServices bigQueryServices;
  private final CreateDisposition createDisposition;
  private final DynamicDestinations<?, DestinationT> dynamicDestinations;
  private InsertRetryPolicy retryPolicy;
  private boolean extendedErrorInfo;
  private final boolean skipInvalidRows;
  private final boolean ignoreUnknownValues;
  private final boolean ignoreInsertIds;
  private final boolean autoSharding;
  private final boolean skipReshuffle;
  private final String kmsKey;
  private final Coder<ElementT> elementCoder;
  private final SerializableFunction<ElementT, TableRow> toTableRow;
  private final SerializableFunction<ElementT, TableRow> toFailsafeTableRow;
  private final SerializableFunction<ElementT, String> toUniqueId;

  /** Constructor. */
  public StreamingInserts(
      CreateDisposition createDisposition,
      DynamicDestinations<?, DestinationT> dynamicDestinations,
      Coder<ElementT> elementCoder,
      SerializableFunction<ElementT, TableRow> toTableRow,
      SerializableFunction<ElementT, TableRow> toFailsafeTableRow) {
    this(
        createDisposition,
        dynamicDestinations,
        new BigQueryServicesImpl(),
        InsertRetryPolicy.alwaysRetry(),
        false,
        false,
        false,
        false,
        false,
        false,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        null,
        null);
  }

  /** Constructor. */
  private StreamingInserts(
      CreateDisposition createDisposition,
      DynamicDestinations<?, DestinationT> dynamicDestinations,
      BigQueryServices bigQueryServices,
      InsertRetryPolicy retryPolicy,
      boolean extendedErrorInfo,
      boolean skipInvalidRows,
      boolean ignoreUnknownValues,
      boolean ignoreInsertIds,
      boolean autoSharding,
      boolean skipReshuffle,
      Coder<ElementT> elementCoder,
      SerializableFunction<ElementT, TableRow> toTableRow,
      SerializableFunction<ElementT, TableRow> toFailsafeTableRow,
      SerializableFunction<ElementT, String> toUniqueId,
      String kmsKey) {
    this.createDisposition = createDisposition;
    this.dynamicDestinations = dynamicDestinations;
    this.bigQueryServices = bigQueryServices;
    this.retryPolicy = retryPolicy;
    this.extendedErrorInfo = extendedErrorInfo;
    this.skipInvalidRows = skipInvalidRows;
    this.ignoreUnknownValues = ignoreUnknownValues;
    this.ignoreInsertIds = ignoreInsertIds;
    this.autoSharding = autoSharding;
    this.skipReshuffle = skipReshuffle;
    this.elementCoder = elementCoder;
    this.toTableRow = toTableRow;
    this.toFailsafeTableRow = toFailsafeTableRow;
    this.toUniqueId = toUniqueId;
    this.kmsKey = kmsKey;
  }

  /** Specify a retry policy for failed inserts. */
  public StreamingInserts<DestinationT, ElementT> withInsertRetryPolicy(
      InsertRetryPolicy retryPolicy) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  /** Specify whether to use extended error info or not. */
  public StreamingInserts<DestinationT, ElementT> withExtendedErrorInfo(boolean extendedErrorInfo) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withSkipInvalidRows(boolean skipInvalidRows) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withIgnoreUnknownValues(boolean ignoreUnknownValues) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withIgnoreInsertIds(boolean ignoreInsertIds) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withAutoSharding(boolean autoSharding) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withSkipReshuffle(boolean skipReshuffle) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withToUniqueId(
      SerializableFunction<ElementT, String> toUniqueId) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withKmsKey(String kmsKey) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  StreamingInserts<DestinationT, ElementT> withTestServices(BigQueryServices bigQueryServices) {
    return new StreamingInserts<>(
        createDisposition,
        dynamicDestinations,
        bigQueryServices,
        retryPolicy,
        extendedErrorInfo,
        skipInvalidRows,
        ignoreUnknownValues,
        ignoreInsertIds,
        autoSharding,
        skipReshuffle,
        elementCoder,
        toTableRow,
        toFailsafeTableRow,
        toUniqueId,
        kmsKey);
  }

  @Override
  public WriteResult expand(PCollection<KV<DestinationT, ElementT>> input) {
    PCollection<KV<TableDestination, ElementT>> writes =
        input.apply(
            "CreateTables",
            new CreateTables<DestinationT, ElementT>(createDisposition, dynamicDestinations)
                .withTestServices(bigQueryServices)
                .withKmsKey(kmsKey));

    return writes.apply(
        new StreamingWriteTables<ElementT>()
            .withTestServices(bigQueryServices)
            .withInsertRetryPolicy(retryPolicy)
            .withExtendedErrorInfo(extendedErrorInfo)
            .withSkipInvalidRows(skipInvalidRows)
            .withIgnoreUnknownValues(ignoreUnknownValues)
            .withIgnoreInsertIds(ignoreInsertIds)
            .withAutoSharding(autoSharding)
            .withSkipReshuffle(skipReshuffle)
            .withElementCoder(elementCoder)
            .withToTableRow(toTableRow)
            .withToFailsafeTableRow(toFailsafeTableRow)
            .withToUniqueId(toUniqueId));
  }
}
