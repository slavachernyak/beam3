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
package org.apache.beam.sdk.extensions.ml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.api.client.json.GenericJson;
import com.google.cloud.recommendationengine.v1beta1.CatalogItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RecommendationAICatalogItemIT {
  @Rule public TestPipeline testPipeline = TestPipeline.create();

  private static GenericJson getCatalogItem() {
    List<Object> categories = new ArrayList<Object>();
    categories.add(new GenericJson().set("categories", Arrays.asList("Electronics", "Computers")));
    categories.add(new GenericJson().set("categories", Arrays.asList("Laptops")));
    return new GenericJson()
        .set("id", Integer.toString(new Random().nextInt()))
        .set("title", "Sample Laptop")
        .set("description", "Indisputably the most fantastic laptop ever created.")
        .set("categoryHierarchies", categories)
        .set("languageCode", "en");
  }

  @Ignore("https://issues.apache.org/jira/browse/BEAM-12733")
  @Test
  public void createCatalogItem() {
    String projectId = testPipeline.getOptions().as(GcpOptions.class).getProject();
    GenericJson catalogItem = getCatalogItem();

    PCollectionTuple createCatalogItemResult =
        testPipeline
            .apply(
                Create.of(Arrays.asList(catalogItem))
                    .withCoder(GenericJsonCoder.of(GenericJson.class)))
            .apply(RecommendationAIIO.createCatalogItems().withProjectId(projectId));
    PAssert.that(createCatalogItemResult.get(RecommendationAICreateCatalogItem.SUCCESS_TAG))
        .satisfies(new VerifyCatalogItemResult(1, (String) catalogItem.get("id")));
    testPipeline.run().waitUntilFinish();
  }

  @Ignore("Import method causing issues")
  @Test
  public void importCatalogItems() {
    String projectId = testPipeline.getOptions().as(GcpOptions.class).getProject();
    ArrayList<KV<String, GenericJson>> catalogItems = new ArrayList<>();

    GenericJson catalogItem1 = getCatalogItem();
    GenericJson catalogItem2 = getCatalogItem();

    catalogItems.add(KV.of(Integer.toString(new Random().nextInt()), catalogItem1));
    catalogItems.add(KV.of(Integer.toString(new Random().nextInt()), catalogItem2));

    PCollectionTuple importCatalogItemResult =
        testPipeline
            .apply(Create.of(catalogItems))
            .apply(RecommendationAIImportCatalogItems.newBuilder().setProjectId(projectId).build());
    PAssert.that(importCatalogItemResult.get(RecommendationAIImportCatalogItems.SUCCESS_TAG))
        .satisfies(new VerifyCatalogItemResult(2, (String) catalogItem1.get("id")));
    testPipeline.run().waitUntilFinish();
  }

  private static class VerifyCatalogItemResult
      implements SerializableFunction<Iterable<CatalogItem>, Void> {

    String catalogItemId;
    int size;

    private VerifyCatalogItemResult(int size, String catalogItemId) {
      this.size = size;
      this.catalogItemId = catalogItemId;
    }

    @Override
    public Void apply(Iterable<CatalogItem> input) {
      List<String> matches = new ArrayList<>();
      input.forEach(
          item -> {
            CatalogItem result = item;
            matches.add(result.getId());
          });
      assertTrue(matches.contains(this.catalogItemId));
      assertEquals(size, matches.size());
      return null;
    }
  }
}
