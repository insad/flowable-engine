/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.test.bpmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.junit.jupiter.api.Test;

/**
 * @author Joram Barrez
 */
public class StartToEndTest extends PluggableFlowableTestCase {

    @Test
    @Deployment
    public void testStartToEnd() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd");
        assertProcessEnded(processInstance.getId());
        assertThat(processInstance.isEnded()).isTrue();
    }

    @Test
    @Deployment(resources = { "org/flowable/engine/test/bpmn/StartToEndTest.testStartToEnd.bpmn20.xml" })
    public void testStartProcessInstanceWithVariables() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("test", "hello");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd", varMap);
        assertProcessEnded(processInstance.getId());
        Map<String, Object> returnVarMap = ((ExecutionEntity) processInstance).getVariables();
        assertThat(returnVarMap).containsEntry("test", "hello");
    }

    @Test
    @Deployment(resources = { "org/flowable/engine/test/bpmn/StartToEndTest.testStartWithServiceTask.bpmn20.xml" })
    public void testStartProcessInstanceWithServiceTask() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("test", "hello");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd", varMap);
        assertProcessEnded(processInstance.getId());
        Map<String, Object> returnVarMap = ((ExecutionEntity) processInstance).getVariables();
        assertThat(returnVarMap)
                .contains(
                        entry("test", "hello"),
                        entry("string", "string"),
                        entry("boolean", true),
                        entry("double", 25.5),
                        entry("long", 10L)
                );
    }

    @Test
    @Deployment(resources = { "org/flowable/engine/test/bpmn/StartToEndTest.testStartWithSerializableVariables.bpmn20.xml" })
    public void testStartProcessInstanceWithSerializableVariables() {
        Map<String, Object> varMap = new HashMap<>();
        varMap.put("test", "hello");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("startToEnd", varMap);
        assertProcessEnded(processInstance.getId());
        Map<String, Object> returnVarMap = ((ExecutionEntity) processInstance).getVariables();
        assertThat(returnVarMap).containsEntry("test", "hello");
        Person person1 = (Person) returnVarMap.get("person1");
        assertThat(person1.getId()).isEqualTo("1");
        assertThat(person1.getName()).isEqualTo("John");
        Person person2 = (Person) returnVarMap.get("person2");
        assertThat(person2.getId()).isEqualTo("2");
        assertThat(person2.getName()).isEqualTo("Paul");
    }

    public static class PrimitiveServiceTaskDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            execution.setVariable("string", "string");
            execution.setVariable("boolean", true);
            execution.setVariable("double", 25.5);
            execution.setVariable("long", 10L);
        }

    }

    public static class SerializableServiceTaskDelegate implements JavaDelegate {

        @Override
        public void execute(DelegateExecution execution) {
            execution.setVariable("person1", new Person("1", "John"));
            execution.setVariable("person2", new Person("2", "Paul"));
        }

    }

    public static class Person implements Serializable {
        private static final long serialVersionUID = 1L;

        protected String id;
        protected String name;

        public Person(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
