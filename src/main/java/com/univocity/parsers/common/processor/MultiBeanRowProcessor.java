/*
 * Copyright (c) 2015. uniVocity Software Pty Ltd
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.univocity.parsers.common.processor;

import com.univocity.parsers.common.*;

import java.util.*;


/**
 *
 * A {@link RowProcessor} implementation for converting rows extracted from any implementation of {@link AbstractParser} into java objects.
 *
 * <p>The class types passed to the constructor of this class must contain the annotations provided in {@link com.univocity.parsers.annotations}.
 *
 * <p> For each row processed, one or more java bean instances of any given class will be created with their fields populated.
 * <p> Once all beans are populated from an individual input record, they will be sent to through the {@link MultiBeanRowProcessor#rowProcessed(Map, ParsingContext)} method,
 * where the user can access all beans parsed for that row.
 *
 * @see AbstractParser
 * @see RowProcessor
 * @see BeanProcessor
 * @see MultiBeanProcessor
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 *
 */
public abstract class MultiBeanRowProcessor extends MultiBeanProcessor {

	private final HashMap<Class<?>, Object> row = new HashMap<Class<?>, Object>();
	private long record = -1L;

	/**
	 * Creates a processor for java beans of multiple types
	 * @param beanTypes the classes with their attributes mapped to fields of records parsed by an {@link AbstractParser} or written by an {@link AbstractWriter}.
	 */
	public MultiBeanRowProcessor(Class... beanTypes) {
		super(beanTypes);
	}

	public void processStarted(ParsingContext context) {
		record = -1L;
		row.clear();
		super.processStarted(context);
	}

	@Override
	public final void beanProcessed(Class<?> beanType, Object beanInstance, ParsingContext context) {
		if(record != context.currentRecord() && record != -1L){
			submitRow(context);
		}
		record = context.currentRecord();
		row.put(beanType, beanInstance);
	}

	private void submitRow(ParsingContext context){
		if(!row.isEmpty()){
			rowProcessed(row, context);
			row.clear();
		}
	}

	@Override
	public void processEnded(ParsingContext context) {
		submitRow(context);
		super.processEnded(context);
	}

	/**
	 * Invoked by the processor after all beans of a valid record have been processed.
	 * @param row a map containing all object instances generated from an input row. <b>The map is reused internally. Make a copy if you want to keep the map</b>.
	 * @param context A contextual object with information and controls over the current state of the parsing process
	 */
	protected abstract void rowProcessed(Map<Class<?>, Object> row, ParsingContext context);
}