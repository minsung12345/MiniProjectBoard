package com.miniproj.persistence;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/spring/**/root-context.xml"})
public class HBoardDAOTest {
	
	@Inject
	private HBoardDAO dao;
	
	@Test
	public void selectAllBoardTest() throws Exception {
		System.out.println(dao.selectAllBoard());
	}
}
