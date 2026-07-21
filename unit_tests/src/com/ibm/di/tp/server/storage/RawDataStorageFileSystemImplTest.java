package com.ibm.di.tp.server.storage;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import javax.naming.InvalidNameException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.di.jaxrs.storage.atom.internal.RawDataStorage;
import com.ibm.di.jaxrs.storage.atom.internal.RawDataStorageFileSystemImpl;

public class RawDataStorageFileSystemImplTest {
	
	public static final String PERSISTENCE_DIR_NAME = "persistence_test_dir";
	
	private static final String TESTKEY = "testnode";
	
	private final File TEMP_DIR = new File("./temp");
	
	private final File PERSISTENCE_DIR = new File(TEMP_DIR, PERSISTENCE_DIR_NAME);
	
	private RawDataStorage storage;

	@Before
	public void before() throws Exception {
		
		// make sure the temporary folder exists
		TEMP_DIR.mkdir();
		
		// make sure the persistence folder exists and is empty
		if (PERSISTENCE_DIR.exists()) {
			recursiveDelete(PERSISTENCE_DIR);
		}
		PERSISTENCE_DIR.mkdir();
		
		storage = new RawDataStorageFileSystemImpl(PERSISTENCE_DIR);
	}
	
	@After
	public void after() throws Exception {
		
		storage.clear();
		storage = null;
		
		// delete the persistence folder
		
		if (PERSISTENCE_DIR.exists()) {
			recursiveDelete(PERSISTENCE_DIR);
		}
	}
	
	@Test
	public void test_getNode_returns_null_for_key_which_does_not_exist() throws Exception {
		assertNull(storage.get(TESTKEY));		
	}
	
	@Test
	public void test_getNode_returns_empty_value_for_key_which_has_empty_content() throws Exception {
		final byte[] expectedData = new byte[0];
		test_get_value(expectedData);
	}
	
	@Test
	public void test_getNode_preserves_value() throws Exception {
		final byte[] expectedData = new byte[] {-10, -5, 0, 0, 9};
		test_get_value(expectedData);
	}
	
	private void test_get_value(byte[] expectedData) throws Exception {
		storage.put(TESTKEY, expectedData);	
		byte[] actualData = storage.get(TESTKEY);
		assertThat(expectedData, is(equalTo(actualData)));
	}
	
	@Test
	public void test_get_preserves_key_values() throws Exception {
		
		final byte[] parentData = new byte[] {1};
		final byte[] nodeData = new byte[] {2, 1};
		final byte[] siblingData = new byte[] {2, 2, 1};
		final byte[] childData = new byte[] {3, 2, 1};
		
		storage.put("parentnode", parentData);
		storage.put("parentnode/testnode", nodeData);
		storage.put("parentnode/siblingnode", siblingData);
		storage.put("parentnode/testnode/childnode", childData);
		
		for (int i = 0; i < 5; ++i) {
			assertThat(parentData, is(equalTo(storage.get("parentnode"))));
			assertThat(nodeData, is(equalTo(storage.get("parentnode/testnode"))));
			assertThat(siblingData, is(equalTo(storage.get("parentnode/siblingnode"))));
			assertThat(childData, is(equalTo(storage.get("parentnode/testnode/childnode"))));
		}
	}
	
	@Test
	public void test_put_creates_key_with_specified_value() throws Exception {
		final byte[] expectedData = new byte[] {0, 0, 1, -125, 0, 0,  1};
		storage.put(TESTKEY, expectedData);	
		byte[] actualData = storage.get(TESTKEY);
		assertThat(expectedData, is(equalTo(actualData)));
	}
	
	@Test
	public void test_put_overwrites_the_value_of_existing_key_with_null_data() throws Exception {
		final byte[] oldData = new byte[] {-1, 1, -2, 2, -3, 3, 4};
		final byte[] expectedData = null;
		test_put_overwrites_the_value_of_existing_key_with_data(oldData, expectedData);
	}
	
	@Test
	public void test_put_overwrites_the_value_of_existing_key_with_empty_data() throws Exception {
		final byte[] oldData = new byte[] {-1, 1, -2, 2, -3, 3, 4};
		final byte[] expectedData = new byte[0];
		test_put_overwrites_the_value_of_existing_key_with_data(oldData, expectedData);
	}
	
	@Test
	public void test_put_overwrites_the_value_of_existing_key_with_non_empty_data() throws Exception {
		final byte[] oldData = new byte[] {-1, 1, -2, 2, -3, 3, 4};
		final byte[] expectedData = new byte[] {0, 1, 0, 0, 1, 0, 0, 0, 1};
		test_put_overwrites_the_value_of_existing_key_with_data(oldData, expectedData);
	}
	
	private void test_put_overwrites_the_value_of_existing_key_with_data(byte[] oldData, byte[] expectedData) throws Exception {
		storage.put(TESTKEY, oldData);	
		storage.put(TESTKEY, expectedData);
		byte[] actualData = storage.get(TESTKEY);
		assertThat(expectedData, is(equalTo(actualData)));
	}
	
	@Test
	public void test_put_creates_intermediate_keys_with_null_values_along_the_path() throws Exception {
		storage.put("parentnode/testnode", null);
		assertNull(storage.get("parentnode"));
	}
	
	@Test
	public void test_put_preserves_value_of_parent_key() throws Exception {
		final byte[] parentData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", parentData);
		
		storage.put("parentnode/testnode", null);
		
		assertThat(parentData, is(equalTo(storage.get("parentnode"))));
	}
	
	@Test
	public void test_put_preserves_value_of_sibling_key() throws Exception {
		final byte[] siblingData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", new byte[] {1});
		storage.put("parentnode/testnode", new byte[] {1});
		storage.put("parentnode/siblingnode", siblingData);
		
		storage.put("parentnode/testnode", new byte[] {2});
		
		assertThat(siblingData, is(equalTo(storage.get("parentnode/siblingnode"))));
	}
	
	@Test
	public void test_put_preserves_value_of_child_key() throws Exception {
		final byte[] childData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", new byte[] {1});
		storage.put("parentnode/testnode", new byte[] {1});
		storage.put("parentnode/testnode/childnode", childData);
		
		storage.put("parentnode/testnode", new byte[] {2});
		
		assertThat(childData, is(equalTo(storage.get("parentnode/testnode/childnode"))));
	}
	
	@Test
	public void test_remove_deletes_key_with_empty_value() throws Exception {
		test_remove_deletes_key_with_value(new byte[0]);
	}
	
	@Test
	public void test_remove_deletes_key_with_non_empty_data() throws Exception {
		test_remove_deletes_key_with_value(new byte[] {0, 1, 2});
	}
	
	private void test_remove_deletes_key_with_value(byte[] value) throws Exception {
		storage.put(TESTKEY, value);
		storage.remove(TESTKEY);
		assertNull(storage.get(TESTKEY));
	}

	@Test
	public void test_remove_preserves_value_of_parent_key() throws Exception {
		final byte[] parentData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", parentData);
		storage.put("parentnode/testnode", new byte[]{1});
		
		storage.remove("parentnode/testnode");
		
		assertThat(parentData, is(equalTo(storage.get("parentnode"))));
	}
	
	@Test
	public void test_remove_preserves_value_of_sibling_key() throws Exception {
		final byte[] siblingData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", new byte[] {1});
		storage.put("parentnode/testnode", new byte[] {1});
		storage.put("parentnode/siblingnode", siblingData);
		
		storage.remove("parentnode/testnode");
		
		assertThat(siblingData, is(equalTo(storage.get("parentnode/siblingnode"))));
	}
	
	@Test
	public void test_remove_preserves_value_of_child_key() throws Exception {
		final byte[] childData = new byte[] {0, -5, -9, -7, 8};
		storage.put("parentnode", new byte[] {1});
		storage.put("parentnode/testnode", new byte[] {1});
		storage.put("parentnode/testnode/childnode", childData);
		
		storage.remove("parentnode/testnode");
		
		assertThat(childData, is(equalTo(storage.get("parentnode/testnode/childnode"))));
	}
	
	@Test
	public void test_remove_does_not_throw_for_non_existing_key() throws Exception {
		storage.remove("nosuchnode");
	}
	
	@Test
	public void test_remove_preserves_values_of_existing_keys_along_the_path_of_key_which_does_not_exist() throws Exception {
		final byte[] parentData = new byte[] {-1, -16};
		storage.put("parentnode", parentData);
		
		storage.remove("parentnode/nosuchnode");
		
		assertThat(parentData, is(equalTo(storage.get("parentnode"))));
	}

	@Test
	public void test_clear_deletes_all_files_in_the_persistence_folder() throws Exception {
		final byte[] parentData = new byte[] {1};
		final byte[] nodeData = new byte[] {2, 1};
		final byte[] childData = new byte[] {3, 2, 1};
		final byte[] grandchildData = new byte[] {3, 2, 1};
		storage.put("parentnode", parentData);
		storage.put("parentnode/testnode", nodeData);
		storage.put("parentnode/testnode/childnode", childData);
		storage.put("parentnode/testnode/childnode/grandchildnode", grandchildData);
		
		storage.clear();
		
		assertNull(PERSISTENCE_DIR.listFiles());
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_null_key_is_not_allowed() throws Exception {
		storage.get(null);
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_empty_key_is_not_allowed() throws Exception {
		storage.get("");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_empty_key_component_is_not_allowed() throws Exception {
		storage.get("/");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_empty_key_component_is_not_allowed_2() throws Exception {
		storage.get("//");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_empty_key_component_is_not_allowed_3() throws Exception {
		storage.get("///");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_single_dot_key_component_is_not_allowed() throws Exception {
		storage.get("./node");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_double_dot_key_component_is_not_allowed() throws Exception {
		storage.get("../node");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_opening_curly_bracket_is_not_allowed() throws Exception {
		storage.get("a{b");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_backslash_is_not_allowed() throws Exception {
		storage.get("\\");
	}
	
	@Test(expected=InvalidNameException.class)
	public void test_get_closing_curly_bracket_is_not_allowed() throws Exception {
		storage.get("a}b");
	}
	
	private void recursiveDelete(File f) throws IOException {
		if (!f.exists()) {
			return;
		}
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				recursiveDelete(child);
			}
		}
		f.delete();
	}
}
