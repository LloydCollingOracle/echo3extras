/* 
 * This file is part of the Echo Extras Project.
 * Copyright (C) 2005-2009 NextApp, Inc.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 */

package nextapp.echo.extras.app.tree;

import java.io.Serializable;

/**
 * Represents a path to a node. A TreePath is an array of Objects that are
 * vended from a TreeModel. The elements of the array are ordered such
 * that the root is always the first element (index 0) of the array.
 */
public class TreePath 
implements Serializable {
    
    /**
     * Path representing the parent, null if lastPathComponent represents the
     * root.
     */
    private TreePath parentPath;

    /** Last path component. */
    private Serializable lastPathComponent;

    /**
     * Constructs a path from an array of Objects, uniquely identifying the path
     * from the root of the tree to a specific node, as returned by the tree's
     * data model.
     * <p>
     * The model is free to return an array of any Objects it needs to represent
     * the path. The DefaultTreeModel returns an array of TreeNode objects. The
     * first TreeNode in the path is the root of the tree, the last TreeNode is
     * the node identified by the path.
     * 
     * @param path
     *            an array of Objects representing the path to a node
     */
    public TreePath(Serializable[] path) {
        if (path == null || path.length == 0) {
            throw new IllegalArgumentException("path in TreePath must be non null and not empty.");
        }
        lastPathComponent = path[path.length - 1];
        if (path.length > 1) {
            parentPath = new TreePath(path, path.length - 1);
        }
    }

    /**
     * Constructs a TreePath containing only a single element. This is usually
     * used to construct a TreePath for the the root of the TreeModel.
     * <p>
     * 
     * @param singlePath
     *            an Object representing the path to a node
     * @see #TreePath(Object[])
     */
    public TreePath(Serializable singlePath) {
        if (singlePath == null) {
            throw new IllegalArgumentException("path in TreePath must be non null.");
        }
        lastPathComponent = singlePath;
        parentPath = null;
    }

    /**
     * Constructs a new TreePath, which is the path identified by
     * <code>parent</code> ending in <code>lastElement</code>.
     */
    protected TreePath(TreePath parent, Serializable lastElement) {
        if (lastElement == null) {
            throw new IllegalArgumentException("path in TreePath must be non null.");
        }
        parentPath = parent;
        lastPathComponent = lastElement;
    }

    /**
     * Constructs a new TreePath with the identified path components of length
     * <code>length</code>.
     */
    protected TreePath(Serializable[] path, int length) {
        lastPathComponent = path[length - 1];
        if (length > 1) {
            parentPath = new TreePath(path, length - 1);
        }
    }

    /**
     * Primarily provided for subclasses that represent paths in a different
     * manner. If a subclass uses this constructor, it should also override the
     * <code>getPath</code>, <code>getPathCount</code>, and
     * <code>getPathComponent</code> methods, and possibly the
     * <code>equals</code> method.
     */
    protected TreePath() {
    }

    /**
     * Returns an ordered array of Objects containing the components of this
     * TreePath. The first element (index 0) is the root.
     * 
     * @return an array of Objects representing the TreePath
     * @see #TreePath(Object[])
     */
    public Serializable[] getPath() {
        int i = getPathCount();
        Serializable[] result = new Serializable[i--];

        for (TreePath path = this; path != null; path = path.parentPath) {
            result[i--] = path.lastPathComponent;
        }
        return result;
    }

    /**
     * Returns the last component of this path. For a path returned by
     * DefaultTreeModel this will return an instance of TreeNode.
     * 
     * @return the Object at the end of the path
     * @see #TreePath(Object[])
     */
    public Serializable getLastPathComponent() {
        return lastPathComponent;
    }

    /**
     * Returns the number of elements in the path.
     * 
     * @return an int giving a count of items the path
     */
    public int getPathCount() {
        int result = 0;
        for (TreePath path = this; path != null; path = path.parentPath) {
            result++;
        }
        return result;
    }

    /**
     * Returns the path component at the specified index.
     * 
     * @param element
     *            an int specifying an element in the path, where 0 is the first
     *            element in the path
     * @return the Object at that index location
     * @throws IllegalArgumentException
     *             if the index is beyond the length of the path
     * @see #TreePath(Object[])
     */
    public Serializable getPathComponent(int element) {
        int pathLength = getPathCount();

        if (element < 0 || element >= pathLength) {
            throw new IllegalArgumentException("Index " + element + " is out of the specified range");
        }

        TreePath path = this;

        for (int i = pathLength - 1; i != element; i--) {
            path = path.parentPath;
        }
        return path.lastPathComponent;
    }

    /**
     * Tests two TreePaths for equality by checking each element of the paths
     * for equality. Two paths are considered equal if they are of the same
     * length, and contain the same elements (<code>.equals</code>).
     * 
     * @param o
     *            the Object to compare
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof TreePath) {
            TreePath oTreePath = (TreePath) o;

            if (getPathCount() != oTreePath.getPathCount()) {
                return false;
            }
            for (TreePath path = this; path != null; path = path.parentPath) {
                if (!(path.lastPathComponent.equals(oTreePath.lastPathComponent))) {
                    return false;
                }
                oTreePath = oTreePath.parentPath;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the hashCode for the object. The hash code of a TreePath is
     * defined to be the hash code of the last component in the path.
     * 
     * @return the hashCode for the object
     */
    public int hashCode() {
        return lastPathComponent.hashCode();
    }

    /**
     * Returns true if <code>aTreePath</code> is a descendant of this
     * TreePath. A TreePath P1 is a descendant of a TreePath P2 if P1 contains
     * all of the components that make up P2's path. For example, if this object
     * has the path [a, b], and <code>aTreePath</code> has the path [a, b, c],
     * then <code>aTreePath</code> is a descendant of this object. However, if
     * <code>aTreePath</code> has the path [a], then it is not a descendant of
     * this object. By this definition a TreePath is always considered a
     * descendant of itself. That is,
     * <code>aTreePath.isDescendant(aTreePath)</code> returns true.
     * 
     * @return true if <code>aTreePath</code> is a descendant of this path
     */
    public boolean isDescendant(TreePath aTreePath) {
        if (aTreePath == this) {
            return true;
        }

        if (aTreePath != null) {
            int pathLength = getPathCount();
            int oPathLength = aTreePath.getPathCount();

            if (oPathLength < pathLength) {
                // Can't be a descendant, has fewer components in the path.
                return false;
            }
            while (oPathLength-- > pathLength) {
                aTreePath = aTreePath.getParentPath();
            }
            return equals(aTreePath);
        }
        return false;
    }

    /**
     * Returns a new path containing all the elements of this object plus
     * <code>child</code>. <code>child</code> will be the last element of
     * the newly created TreePath. This will throw a NullPointerException if
     * child is null.
     */
    public TreePath pathByAddingChild(Serializable child) {
        if (child == null) {
            throw new NullPointerException("Null child not allowed");
        }

        return new TreePath(this, child);
    }

    /**
     * Returns a path containing all the elements of this object, except the
     * last path component.
     */
    public TreePath getParentPath() {
        return parentPath;
    }

    /**
     * Returns a string that displays and identifies this object's properties.
     * 
     * @return a String representation of this object
     */
    public String toString() {
        StringBuffer tempSpot = new StringBuffer("[");

        for (int counter = 0, maxCounter = getPathCount(); counter < maxCounter; counter++) {
            if (counter > 0) {
                tempSpot.append(", ");
            }
            tempSpot.append(getPathComponent(counter));
        }
        tempSpot.append("]");
        return tempSpot.toString();
    }
}
