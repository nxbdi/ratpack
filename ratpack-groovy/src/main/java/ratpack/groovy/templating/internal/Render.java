/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.groovy.templating.internal;

import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import ratpack.func.Action;
import ratpack.util.Result;
import ratpack.func.Transformer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static ratpack.util.ExceptionUtils.toException;
import static ratpack.util.ExceptionUtils.uncheck;

public class Render {

  private final LoadingCache<TemplateSource, CompiledTemplate> compiledTemplateCache;
  private final Transformer<String, TemplateSource> includeTransformer;

  public Render(ByteBufAllocator byteBufAllocator, LoadingCache<TemplateSource, CompiledTemplate> compiledTemplateCache, TemplateSource templateSource, Map<String, ?> model, final Action<Result<ByteBuf>> handler, Transformer<String, TemplateSource> includeTransformer) throws Exception {
    this.compiledTemplateCache = compiledTemplateCache;
    this.includeTransformer = includeTransformer;

    ByteBuf byteBuf = byteBufAllocator.buffer();

    try {
      execute(getFromCache(compiledTemplateCache, templateSource), model, byteBuf);
    } catch (Exception e) {
      handler.execute(new Result<ByteBuf>(e));
      return;
    }

    //noinspection Convert2Diamond
    handler.execute(new Result<ByteBuf>(byteBuf));
  }

  private CompiledTemplate getFromCache(LoadingCache<TemplateSource, CompiledTemplate> compiledTemplateCache, TemplateSource templateSource) {
    try {
      return compiledTemplateCache.get(templateSource);
    } catch (ExecutionException | UncheckedExecutionException e) {
      throw uncheck(toException(e.getCause()));
    }
  }

  private void executeNested(final String templatePath, final Map<String, ?> model, ByteBuf buffer) throws Exception {
    TemplateSource templateSource = includeTransformer.transform(templatePath);
    CompiledTemplate compiledTemplate = getFromCache(compiledTemplateCache, templateSource);
    execute(compiledTemplate, model, buffer);
  }

  private void execute(CompiledTemplate compiledTemplate, final Map<String, ?> model, final ByteBuf parts) throws Exception {
    compiledTemplate.execute(model, parts, new NestedRenderer() {
      public void render(String templatePath, Map<String, ?> nestedModel) throws Exception {
        Map<String, Object> modelCopy = new HashMap<>(model);
        modelCopy.putAll(nestedModel);
        executeNested(templatePath, modelCopy, parts);
      }
    });
  }

}