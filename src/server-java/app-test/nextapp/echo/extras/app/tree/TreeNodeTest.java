package nextapp.echo.extras.app.tree;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * A set of tests for the TreeNode related classes.
 *
 * @author Lloyd Colling
 */
public class TreeNodeTest extends TestCase {

	/**
	 * Tests that the addChild method of treeNode works as expected
	 */
	public void testAddChild() {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode();
		DefaultMutableTreeNode child = new DefaultMutableTreeNode();

		assertEquals(parent.getChildCount(), 0);
		assertNull(child.getParent());

		parent.addChild(child);

		assertEquals(parent.getChildCount(), 1);
		assertEquals(parent.getChild(0), child);
		assertEquals(child.getParent(), parent);
	}

	/**
	 * Tests the the getChildCount method of treeNode works as expected
	 */
	public void testGetChildCount() {
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode();
		assertEquals(parent.getChildCount(), 0);
		parent.addChild(new DefaultMutableTreeNode());
		assertEquals(parent.getChildCount(), 1);
		parent.removeAllChildren();
		assertEquals(parent.getChildCount(), 0);
	}

	/**
	 * Tests the the get column values method works as expected
	 */
	public void testGetColumnValues() {
		Map columnValues = new HashMap();
		columnValues.put("aNumber", new Integer(1));
		columnValues.put("aBoolean", Boolean.FALSE);
		columnValues.put("aString", "BLAH");
		DefaultMutableTreeNode parent = new DefaultMutableTreeNode(columnValues);

		assertNotNull(parent.getColumnValues());
		assertEquals(parent.getColumnValues().size(), columnValues.size());
		assertTrue(parent.getColumnValues().entrySet()
				.containsAll(columnValues.entrySet()));
	}

	/**
	 * Tests that the get node for path method works as expected
	 */
	public void testGetNodeForPath() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node3 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node4 = new DefaultMutableTreeNode();

		node1.addChild(node2);
		node1.addChild(node3);
		node3.addChild(node4);

		assertEquals(
				node1.getNodeForPath(new TreePath(new Object[] { node1 })),
				node1);
		assertEquals(
				node1.getNodeForPath(new TreePath(new Object[] { node1, node3,
						node4 })), node4);

		try {
			node1.getNodeForPath(new TreePath(new Object[0]));
			fail("get node for path is not checking the path has entries");
		} catch (IllegalArgumentException e) {
			// this is what we expect to happen
		}

		try {
			node1.getNodeForPath(new TreePath(new Object[] { node3, node4 }));
			fail("get node for path is not checking the first node in the path");
		} catch (IllegalStateException e) {
			// this is what we expect to happen
		}
	}

	/**
	 * Tests that the getParent method of tree node works as expected
	 */
	public void testGetParent() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode();
		assertNull(node1.getParent());
		assertNull(node2.getParent());

		node1.addChild(node2);
		assertNull(node1.getParent());
		assertNotNull(node2.getParent());

		node1.removeChild(node2);
		assertNull(node2.getParent());

		node1.addChild(node2);
		node1.removeAllChildren();
		assertNull(node2.getParent());
	}

	public void testIsLeaf() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		assertTrue(node1.isLeaf());

		node1.setLeaf(Boolean.FALSE);

		assertFalse(node1.isLeaf());

		node1.setLeaf(null);

		assertTrue(node1.isLeaf());

		node1.addChild(new DefaultMutableTreeNode());

		assertFalse(node1.isLeaf());
	}

	public void testRemoveAllChildren() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		assertEquals(node1.getChildCount(), 0);

		try {
			node1.removeAllChildren();
		} catch (Throwable e) {
			fail("Call to remove all children threw an exception: "
					+ e.getMessage());
		}

		node1.addChild(new DefaultMutableTreeNode());
		assertEquals(node1.getChildCount(), 1);
		node1.removeAllChildren();
		assertEquals(node1.getChildCount(), 0);
	}

	public void testRemoveChild() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode();
		node1.addChild(node2);

		assertEquals(node1.getChildCount(), 1);

		node1.removeChild(node2);

		assertEquals(node1.getChildCount(), 0);

		try {
			node1.removeChild(node2);
		} catch (Throwable t) {
			fail("Call to remove child threw an exception: " + t.getMessage());
		}
	}

	public void testSetColumnValues() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();

		assertNotNull(node1.getColumnValues());
		assertEquals(node1.getColumnValues().size(), 0);

		Map columnValues = new HashMap();
		columnValues.put("aNumber", new Integer(1));
		columnValues.put("aBoolean", Boolean.FALSE);
		columnValues.put("aString", "BLAH");

		node1.setColumnValues(columnValues);

		assertNotNull(node1.getColumnValues());
		assertEquals(node1.getColumnValues().size(), columnValues.size());
	}

	public void testGetIndexOf() {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode();

		assertEquals(node1.getIndexOf(node2), -1);

		node1.addChild(node2);

		assertEquals(node1.getIndexOf(node2), 0);

		node1.removeChild(node2);

		assertEquals(node1.getIndexOf(node2), -1);
	}

	public void testCreateModelFromNodes() {

		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode(
				getMap(new String[] { "node1", "node1" }));
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(
				getMap(new String[] { "node2", "node2" }));
		DefaultMutableTreeNode node3 = new DefaultMutableTreeNode(
				getMap(new String[] { "node3", "node3" }));
		DefaultMutableTreeNode node4 = new DefaultMutableTreeNode(
				getMap(new String[] { "node4", "node4" }));

		node1.addChild(node2);
		node1.addChild(node3);
		node3.addChild(node4);

		TreeNodeModel model = new TreeNodeModel(node1);

		assertEquals(model.getRoot(), node1);

		assertEquals(model.getChildCount(model.getRoot()),
				node1.getChildCount());
		assertEquals(model.getChild(model.getRoot(), 0), node1.getChild(0));
		assertEquals(model.getChild(model.getRoot(), 1), node1.getChild(1));
		assertEquals(model.isLeaf(model.getRoot()), node1.isLeaf());

		assertEquals(model.isLeaf(node3), node3.isLeaf());

		assertEquals(model.getColumnCount(), 2);

		assertEquals("node3", model.getValueAt(node3, 1));
	}

	private Map getMap(String[] values) {
		Map ret = new HashMap();
		for (int i = 0; i < values.length; i++) {
			ret.put(Integer.toString(i), values[i]);
		}

		return ret;
	}

}
