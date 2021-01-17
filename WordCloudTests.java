package edu.brandeis.cs12b.pa09;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import edu.brandeis.cs12b.pa09.TwitterCloud;

public class WordCloudTests {

	
	@Test
	public void test1() throws InterruptedException{
		TwitterCloud tc = new TwitterCloud();
		tc.makeCloud(new String[] {"donald" , "trump" }, "test.png");
		File file = new File("test.png");
		assertTrue(file.exists());
	}
	
	@Test
	public void test2() throws InterruptedException{
		TwitterCloud tc = new TwitterCloud();
		tc.makeCloud(new String[] {"donald" , "trump" }, "test.png");
		List<String> tokenList = tc.getWords();
		assertTrue(!tokenList.isEmpty());
	}
	
	
}
