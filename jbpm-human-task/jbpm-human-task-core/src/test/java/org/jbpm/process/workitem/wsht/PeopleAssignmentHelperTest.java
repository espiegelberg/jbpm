/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jbpm.process.workitem.wsht;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.process.instance.WorkItem;
import org.drools.process.instance.impl.WorkItemImpl;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Task;
import org.jbpm.task.TaskData;
import org.jbpm.task.User;
import org.junit.Test;

/**
 * @author Eric Spiegelberg
 */
public class PeopleAssignmentHelperTest extends TestCase {
	
	private PeopleAssignmentHelper peopleAssignmentHelper = new PeopleAssignmentHelper();
	
	protected void setup() {
		
		peopleAssignmentHelper = new PeopleAssignmentHelper();
		
	}
	
	@Test
	public void testProcessPeopleAssignments() {

		List<OrganizationalEntity> organizationalEntities = new ArrayList<OrganizationalEntity>();
		
		String ids = "espiegelberg,   drbug   ";
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 2);
		organizationalEntities.contains("drbug");
		organizationalEntities.contains("espiegelberg");
		assertTrue(organizationalEntities.get(0) instanceof User);
		assertTrue(organizationalEntities.get(1) instanceof User);
		
		ids = null;
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 0);
		
		ids = "     ";
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, true);
		assertTrue(organizationalEntities.size() == 0);
		
		ids = "Software Developer";
		organizationalEntities = new ArrayList<OrganizationalEntity>();
		assertTrue(organizationalEntities.size() == 0);		
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, false);
		assertTrue(organizationalEntities.size() == 1);
		assertTrue(organizationalEntities.get(0) instanceof Group);
		
		// Test that a duplicate is not added; only 1 of the 2 passed in should be added
		ids = "Software Developer,Project Manager";
		peopleAssignmentHelper.processPeopleAssignments(ids, organizationalEntities, false);
		assertTrue(organizationalEntities.size() == 2);
		assertTrue(organizationalEntities.get(0) instanceof Group);
		assertTrue(organizationalEntities.get(1) instanceof Group);
		
	}
	
	@Test
	public void testAssignActors() {
		
		String actorId = "espiegelberg";
		
		Task task = new Task();
		TaskData taskData = new TaskData();
		PeopleAssignments peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		
		WorkItem workItem = new WorkItemImpl();		
		workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, actorId);
		
		peopleAssignmentHelper.assignActors(workItem, peopleAssignments, taskData);
		OrganizationalEntity organizationalEntity1 = peopleAssignments.getPotentialOwners().get(0);
		assertTrue(organizationalEntity1 instanceof User);
		assertEquals(actorId, organizationalEntity1.getId());		
		assertEquals(actorId, taskData.getCreatedBy().getId());
		
		workItem = new WorkItemImpl();
		peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);				
		workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, actorId + ", drbug  ");
		peopleAssignmentHelper.assignActors(workItem, peopleAssignments, taskData);
		assertEquals(2, peopleAssignments.getPotentialOwners().size());
		organizationalEntity1 = peopleAssignments.getPotentialOwners().get(0);
		assertEquals(actorId, organizationalEntity1.getId());		
		assertEquals(actorId, taskData.getCreatedBy().getId());
		OrganizationalEntity organizationalEntity2 = peopleAssignments.getPotentialOwners().get(1);
		assertEquals("drbug", organizationalEntity2.getId());

		workItem = new WorkItemImpl();
		peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);				
		workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, "");
		peopleAssignmentHelper.assignActors(workItem, peopleAssignments, taskData);
		assertEquals(0, peopleAssignments.getPotentialOwners().size());

	}
	
	@Test
	public void testAssignBusinessAdministrators() {
	
		String businessAdministratorId = "espiegelberg";
		
		Task task = new Task();
		PeopleAssignments peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		
		WorkItem workItem = new WorkItemImpl();		
		workItem.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, businessAdministratorId);

		peopleAssignmentHelper.assignBusinessAdministrators(workItem, peopleAssignments);
		assertEquals(2, peopleAssignments.getBusinessAdministrators().size());
		OrganizationalEntity organizationalEntity1 = peopleAssignments.getBusinessAdministrators().get(0);
		assertTrue(organizationalEntity1 instanceof User);
		assertEquals("Administrator", organizationalEntity1.getId());
		OrganizationalEntity organizationalEntity2 = peopleAssignments.getBusinessAdministrators().get(1);		
		assertTrue(organizationalEntity2 instanceof User);				
		assertEquals(businessAdministratorId, organizationalEntity2.getId());
		
	}
	
	@Test
	public void testAssignTaskstakeholders() {
	
		String taskStakeholderId = "espiegelberg";
		
		Task task = new Task();
		PeopleAssignments peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		
		WorkItem workItem = new WorkItemImpl();		
		workItem.setParameter(PeopleAssignmentHelper.TASKSTAKEHOLDER_ID, taskStakeholderId);

		peopleAssignmentHelper.assignTaskStakeholders(workItem, peopleAssignments);
		assertEquals(1, peopleAssignments.getTaskStakeholders().size());
		OrganizationalEntity organizationalEntity1 = peopleAssignments.getTaskStakeholders().get(0);		
		assertTrue(organizationalEntity1 instanceof User);				
		assertEquals(taskStakeholderId, organizationalEntity1.getId());
		
	}
	
	@Test
	public void testAssignGroups() {
		
		String groupId = "Software Developers, Project Managers";
		
		Task task = new Task();
		PeopleAssignments peopleAssignments = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		
		WorkItem workItem = new WorkItemImpl();		
		workItem.setParameter(PeopleAssignmentHelper.GROUP_ID, groupId);
		
		peopleAssignmentHelper.assignGroups(workItem, peopleAssignments);
		OrganizationalEntity organizationalEntity1 = peopleAssignments.getPotentialOwners().get(0);
		assertTrue(organizationalEntity1 instanceof Group);
		assertEquals("Software Developers", organizationalEntity1.getId());
		OrganizationalEntity organizationalEntity2 = peopleAssignments.getPotentialOwners().get(1);
		assertTrue(organizationalEntity2 instanceof Group);
		assertEquals("Project Managers", organizationalEntity2.getId());
		
	}
	
	@Test
	public void testgetNullSafePeopleAssignments() {
		
		Task task = new Task();
		
		PeopleAssignments peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		assertNotNull(peopleAssignment);
		
		peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		assertNotNull(peopleAssignment);
		
		task.setPeopleAssignments(null);
		peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		assertNotNull(peopleAssignment);
		assertEquals(0, peopleAssignment.getPotentialOwners().size());
		assertEquals(0, peopleAssignment.getBusinessAdministrators().size());
		assertEquals(0, peopleAssignment.getExcludedOwners().size());
		assertEquals(0, peopleAssignment.getRecipients().size());
		assertEquals(0, peopleAssignment.getTaskStakeholders().size());
		
	}	
	
	@Test
	public void testHandlePeopleAssignments() {
		
		Task task = new Task();
		TaskData taskData = new TaskData();
		PeopleAssignments peopleAssignment = peopleAssignmentHelper.getNullSafePeopleAssignments(task);
		assertNotNull(peopleAssignment);
		assertEquals(0, peopleAssignment.getPotentialOwners().size());
		assertEquals(0, peopleAssignment.getBusinessAdministrators().size());
		assertEquals(0, peopleAssignment.getTaskStakeholders().size());
		
		String actorId = "espiegelberg";
		String taskStakeholderId = "drmary";
		String businessAdministratorId = "drbug";
		
		WorkItem workItem = new WorkItemImpl();		
		workItem.setParameter(PeopleAssignmentHelper.ACTOR_ID, actorId);
		workItem.setParameter(PeopleAssignmentHelper.TASKSTAKEHOLDER_ID, taskStakeholderId);
		workItem.setParameter(PeopleAssignmentHelper.BUSINESSADMINISTRATOR_ID, businessAdministratorId);
		
		peopleAssignmentHelper.handlePeopleAssignments(workItem, task, taskData);
		
		List<OrganizationalEntity> potentialOwners = task.getPeopleAssignments().getPotentialOwners();
		assertEquals(1, potentialOwners.size());
		assertEquals(actorId, potentialOwners.get(0).getId());
		
		List<OrganizationalEntity> businessAdministrators = task.getPeopleAssignments().getBusinessAdministrators();
		assertEquals(2, businessAdministrators.size());
		assertEquals("Administrator", businessAdministrators.get(0).getId());
		assertEquals(businessAdministratorId, businessAdministrators.get(1).getId());
		
		List<OrganizationalEntity> taskStakehoders = task.getPeopleAssignments().getTaskStakeholders();
		assertEquals(1, taskStakehoders.size());
		assertEquals(taskStakeholderId, taskStakehoders.get(0).getId());
		
	}
	
}