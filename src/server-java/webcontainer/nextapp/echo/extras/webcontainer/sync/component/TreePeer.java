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

package nextapp.echo.extras.webcontainer.sync.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import nextapp.echo.app.Component;
import nextapp.echo.app.Window;
import nextapp.echo.app.serial.SerialException;
import nextapp.echo.app.serial.SerialPropertyPeer;
import nextapp.echo.app.update.ClientUpdateManager;
import nextapp.echo.app.update.ServerComponentUpdate;
import nextapp.echo.app.util.Context;
import nextapp.echo.extras.app.Tree;
import nextapp.echo.extras.app.event.TreeExpansionEvent;
import nextapp.echo.extras.app.event.TreeExpansionListener;
import nextapp.echo.extras.app.serial.property.SerialPropertyPeerConstants;
import nextapp.echo.extras.app.tree.TreeModel;
import nextapp.echo.extras.app.tree.TreePath;
import nextapp.echo.extras.app.tree.TreeSelectionModel;
import nextapp.echo.extras.webcontainer.CommonResources;
import nextapp.echo.extras.webcontainer.service.CommonService;
import nextapp.echo.extras.webcontainer.sync.component.tree.TreeRenderState;
import nextapp.echo.extras.webcontainer.sync.component.tree.TreeStructure;
import nextapp.echo.webcontainer.AbstractComponentSynchronizePeer;
import nextapp.echo.webcontainer.ContentType;
import nextapp.echo.webcontainer.RenderState;
import nextapp.echo.webcontainer.ResourceRegistry;
import nextapp.echo.webcontainer.ServerMessage;
import nextapp.echo.webcontainer.Service;
import nextapp.echo.webcontainer.UserInstance;
import nextapp.echo.webcontainer.WebContainerServlet;
import nextapp.echo.webcontainer.service.JavaScriptService;
import nextapp.echo.webcontainer.util.MultiIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TreePeer 
extends AbstractComponentSynchronizePeer {

    /**
     * Holds a selection update from the client
     */
    private static class TreeSelectionUpdate {
        boolean clear = false;
        List addedSelections = new LinkedList();
        List removedSelections = new LinkedList();
    }
    
    /**
     * Translates a selection update directive from the client to 
     * a TreeSelectionUpdatePeer object.
     */
    public static class TreeSelectionUpdatePeer 
    implements SerialPropertyPeer {

        public Object toProperty(Context context, Class objectClass,
                Element propertyElement) throws SerialException {
            TreeSelectionUpdate update = new TreeSelectionUpdate();
            String cStr = propertyElement.getAttribute("c");
            update.clear = Boolean.valueOf(cStr).booleanValue();
            if (propertyElement.hasAttribute("r")) {
                String rStr = propertyElement.getAttribute("r");
                String[] rTokens = rStr.split(",");
                for (int i = 0; i < rTokens.length; i++) {
                    update.removedSelections.add(Integer.valueOf(rTokens[i]));
                }
            }
            if (propertyElement.hasAttribute("a")) {
                String aStr = propertyElement.getAttribute("a");
                String[] aTokens = aStr.split(",");
                for (int i = 0; i < aTokens.length; i++) {
                    update.addedSelections.add(Integer.valueOf(aTokens[i]));
                }
            }
            return update;
        }

        public void toXml(Context context, Class objectClass,
                Element propertyElement, Object propertyValue)
                throws SerialException {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static int getColumnCount(Tree tree) {
        return tree.getColumnModel().getColumnCount();
    }
    
    /**
     * Translates the current selection model to a comma separated string. This string
     * holds the render ids of the nodes. If a node is not yet sent to the client, the
     * selection state will be kept on the client (using the unset selections list of 
     * the render state object).
     * 
     * @param context
     * @param selectionModel
     * @param tree
     * @return the selection string
     */
    private static String getSelectionString(Context context, TreeSelectionModel selectionModel, Tree tree) {
        UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
        TreeRenderState renderState = (TreeRenderState) tree.getContainingWindow().getRenderState(tree);
        
        StringBuffer selection = new StringBuffer();
        TreePath[] paths = selectionModel.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            TreePath path = paths[i];
            Component component = null;
            if (pathIsVisible(path, tree)) {
                component = tree.getComponent(path, 0);
            }
            if (component == null) {
                if (renderState != null) {
                    renderState.addUnsentSelection(path);
                }
            } else {
                String id = userInstance.getClientRenderId(component);
                if (renderState != null) {
                    renderState.removeUnsentSelection(path);
                }
                if (selection.length() > 0) {
                    selection.append(",");
                }
                selection.append(id);
            }
        }
        return selection.toString();
    }
    
	/**
	 * This method determines if the given path is currently visible to the user
	 * @param path
	 * @return
	 */
	private static boolean pathIsVisible(TreePath path, Tree tree) {    	
	    // root node is always visible
	    if (path.getPathCount() == 1)
	        return true;
	    
	    if (tree.isExpanded(path.getParentPath())) {
	        return pathIsVisible(path.getParentPath(), tree);
	    } else {
	        return false;
	    }
	}

    private static final String PROPERTY_TREE_STRUCTURE = "treeStructure";
    private static final String PROPERTY_COLUMN_COUNT = "columnCount";
    private static final String PROPERTY_COLUMN_WIDTH = "columnWidth";
    private static final String PROPERTY_SELECTION_MODE = "selectionMode";
    
    private static final String EXPANSION_PROPERTY = "expansion"; 
    private static final String SELECTION_PROPERTY = "selectionUpdate";
    
    private static final String[] MODEL_CHANGED_UPDATE_PROPERTIES = new String[] { PROPERTY_TREE_STRUCTURE,
            PROPERTY_COLUMN_COUNT};
    
    private static final Service TREE_SERVICE = JavaScriptService.forResources("EchoExtras.RemoteTree",  
            new String[]{ "nextapp/echo/extras/webcontainer/resource/Application.RemoteTree.js",
                    "nextapp/echo/extras/webcontainer/resource/Serial.RemoteTree.js",
                    "nextapp/echo/extras/webcontainer/resource/Sync.RemoteTree.js" });

    static {
        WebContainerServlet.getServiceRegistry().add(TREE_SERVICE);
        CommonResources.install();
        ResourceRegistry resources = WebContainerServlet.getResourceRegistry();
        resources.add("Extras", "image/tree/Transparent.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/Closed.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/Open.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/JoinSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/JoinBottomSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/VerticalSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/ClosedSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/ClosedBottomSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/OpenSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/OpenBottomSolid.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/JoinDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/JoinBottomDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/VerticalDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/ClosedDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/ClosedBottomDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/OpenDotted.gif", ContentType.IMAGE_GIF);
        resources.add("Extras", "image/tree/OpenBottomDotted.gif", ContentType.IMAGE_GIF);
    }

    public TreePeer() {
        super();
        addOutputProperty(PROPERTY_TREE_STRUCTURE);
        addOutputProperty(PROPERTY_COLUMN_COUNT);
        addOutputProperty(PROPERTY_COLUMN_WIDTH, true);
        addOutputProperty(PROPERTY_SELECTION_MODE);
        addOutputProperty(Tree.SELECTION_CHANGED_PROPERTY);
        
        addEvent(new AbstractComponentSynchronizePeer.EventPeer(Tree.INPUT_ACTION, Tree.ACTION_LISTENERS_CHANGED_PROPERTY));
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getComponentClass()
     */
    public Class getComponentClass() {
        return Tree.class;
    }
    
    /**
     * @see nextapp.echo.webcontainer.ComponentSynchronizePeer#getClientComponentType(boolean)
     */
    public String getClientComponentType(boolean mode) {
        return "Extras.RemoteTree";
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getOutputProperty(
     *      nextapp.echo.app.util.Context, nextapp.echo.app.Component, java.lang.String, int)
     */
    public Object getOutputProperty(Context context, Component component, String propertyName, int propertyIndex) {
        Tree tree = (Tree) component;
        if (PROPERTY_TREE_STRUCTURE.equals(propertyName)) {
            return new TreeStructure(tree);
        } else if (PROPERTY_COLUMN_COUNT.equals(propertyName)) {
            return new Integer(getColumnCount(tree));
        } else if (PROPERTY_COLUMN_WIDTH.equals(propertyName)) {
            return tree.getColumnModel().getColumn(propertyIndex).getWidth();
        } else if (PROPERTY_SELECTION_MODE.equals(propertyName)) {
            return new Integer(tree.getSelectionModel().getSelectionMode());
        } else if (Tree.SELECTION_CHANGED_PROPERTY.equals(propertyName)) {
            return getSelectionString(context, tree.getSelectionModel(), tree);
        }
        return super.getOutputProperty(context, component, propertyName, propertyIndex);
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getOutputPropertyIndices(nextapp.echo.app.util.Context,
     *      nextapp.echo.app.Component, java.lang.String)
     */
    public Iterator getOutputPropertyIndices(Context context, Component component, String propertyName) {
        if (PROPERTY_COLUMN_WIDTH.equals(propertyName)) {
            final Iterator columnIterator = ((Tree) component).getColumnModel().getColumns();
            return new Iterator() {
                private int i = 0;
            
                public boolean hasNext() {
                    return columnIterator.hasNext();
                }
            
                public Object next() {
                    columnIterator.next();
                    return new Integer(i++);
                }
            
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return super.getOutputPropertyIndices(context, component, propertyName);
        }
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#getUpdatedOutputPropertyNames(
     *      nextapp.echo.app.util.Context,
     *      nextapp.echo.app.Component,
     *      nextapp.echo.app.update.ServerComponentUpdate)
     */
    public Iterator getUpdatedOutputPropertyNames(Context context, Component component, 
            ServerComponentUpdate update) {
        UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
        
        Iterator normalPropertyIterator = super.getUpdatedOutputPropertyNames(context, component, update);
        HashSet extraProperties = new HashSet();
        
        Tree tree = (Tree) update.getParent();
        if (update.hasRemovedChildren() || update.hasRemovedDescendants()) {
        	tree.getContainingWindow().removeRenderState(component);
            extraProperties.add(PROPERTY_TREE_STRUCTURE);
            extraProperties.add(Tree.SELECTION_CHANGED_PROPERTY);
        }
        
        if (update.hasUpdatedProperty(Tree.MODEL_CHANGED_PROPERTY)) {
            extraProperties.addAll(Arrays.asList(MODEL_CHANGED_UPDATE_PROPERTIES));
        } 
        if (update.hasUpdatedProperty(Tree.EXPANSION_STATE_CHANGED_PROPERTY)) {
            TreeRenderState renderState = (TreeRenderState) tree.getContainingWindow().getRenderState(component);
            if (renderState == null || renderState.hasChangedPaths()) {
                extraProperties.add(PROPERTY_TREE_STRUCTURE);
            }
            if (renderState == null || renderState.hasUnsentSelections()) {
                extraProperties.add(Tree.SELECTION_CHANGED_PROPERTY);
            }
        }
        return new MultiIterator(new Iterator[] { normalPropertyIterator, extraProperties.iterator() });
    }
    
    /**
     * @see nextapp.echo.webcontainer.AbstractComponentSynchronizePeer#storeInputProperty(nextapp.echo.app.util.Context, 
     *      nextapp.echo.app.Component, java.lang.String, int, java.lang.Object)
     */
    public void storeInputProperty(Context context, Component component,
            String propertyName, int index, Object newValue) {
        Tree tree = (Tree) component;
        if (EXPANSION_PROPERTY.equals(propertyName)) {
            int row = ((Integer)newValue).intValue();
            UserInstance userInstance = (UserInstance) context.get(UserInstance.class);
            TreeRenderState renderState = (TreeRenderState) tree.getContainingWindow().getRenderState(component);
            if (renderState == null) {
                renderState = new TreeRenderState(tree);
                tree.getContainingWindow().setRenderState(component, renderState);
            }
            TreePath path = tree.getPathForRow(row);
            renderState.setClientPath(path);
            renderState.removeSentPath(path);
            
            ClientUpdateManager clientUpdateManager = (ClientUpdateManager) context.get(ClientUpdateManager.class);
            clientUpdateManager.setComponentProperty(component, Tree.EXPANSION_STATE_CHANGED_PROPERTY, newValue);
        } else if (SELECTION_PROPERTY.equals(propertyName)) {
            TreeSelectionUpdate update = (TreeSelectionUpdate) newValue;
            TreeSelectionModel selectionModel = tree.getSelectionModel();
            // process deselections
            if (!update.removedSelections.isEmpty()) {
                TreePath[] paths = new TreePath[update.removedSelections.size()];
                int i = 0;
                for (Iterator iterator = update.removedSelections.iterator(); iterator.hasNext();) {
                    Integer row = (Integer) iterator.next();
                    paths[i++] = tree.getPathForRow(row.intValue());
                }
                selectionModel.removeSelectionPaths(paths);
            }
            // process selections
            if (!update.addedSelections.isEmpty()) {
                TreePath[] paths = new TreePath[update.addedSelections.size()];
                int i = 0;
                for (Iterator iterator = update.addedSelections.iterator(); iterator.hasNext();) {
                    Integer row = (Integer) iterator.next();
                    paths[i++] = tree.getPathForRow(row.intValue());
                }
                if (update.clear) {
                    selectionModel.setSelectionPaths(paths);
                } else {
                    selectionModel.addSelectionPaths(paths);
                }
            }
        } else {
            super.storeInputProperty(context, component, propertyName, index, newValue);
        }
    }
    
    public Class getInputPropertyClass(String propertyName) {
        if (EXPANSION_PROPERTY.equals(propertyName)) {
            return Integer.class;
        } else if (SELECTION_PROPERTY.equals(propertyName)) {
            return TreeSelectionUpdate.class;
        }
        return super.getInputPropertyClass(propertyName);
    }
    
    /**
     * @see nextapp.echo.webcontainer.ComponentSynchronizePeer#init(nextapp.echo.app.util.Context, Component)
     */
    public void init(Context context, Component component) {
        super.init(context, component);
        ServerMessage serverMessage = (ServerMessage) context.get(ServerMessage.class);
        serverMessage.addLibrary(CommonService.INSTANCE.getId());
        serverMessage.addLibrary(TREE_SERVICE.getId());
    }
}
