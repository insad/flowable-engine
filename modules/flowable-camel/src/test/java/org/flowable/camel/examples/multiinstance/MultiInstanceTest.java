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

package org.flowable.camel.examples.multiinstance;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.job.api.Job;
import org.flowable.spring.impl.test.SpringFlowableTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Saeid Mirzaei
 */
@Tag("camel")
@ContextConfiguration("classpath:generic-camel-flowable-context.xml")
public class MultiInstanceTest extends SpringFlowableTestCase {

    @Autowired
    protected CamelContext camelContext;

    @BeforeEach
    public void setUp() throws Exception {
        camelContext.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("flowable:miProcessExample:serviceTask1").to("seda:continueAsync1");
                from("seda:continueAsync1").to("bean:sleepBean?method=sleep").to("flowable:miProcessExample:receive1");
            }
        });
    }

    @Test
    @Deployment(resources = { "process/multiinstanceReceive.bpmn20.xml" })
    public void testRunProcess() throws Exception {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("miProcessExample");
        List<Job> jobList = managementService.createJobQuery().list();
        assertEquals(5, jobList.size());

        assertEquals(5, runtimeService.createExecutionQuery()
                .processInstanceId(processInstance.getId())
                .activityId("serviceTask1").count());

        waitForJobExecutorToProcessAllJobs(3000, 500);
        int counter = 0;
        long processInstanceCount = 1;
        while (processInstanceCount == 1 && counter < 20) {
            Thread.sleep(500);
            processInstanceCount = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count();
            counter++;
        }
        assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
    }
}
