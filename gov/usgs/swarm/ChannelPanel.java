package gov.usgs.swarm;

import gov.usgs.util.ConfigFile;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * ChannelPanel is a UI element for selecting which seismic channel to open
 * and how.
 *
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/03/04 23:10:55  dcervelli
 * Made into a JPanel.
 *
 * Revision 1.3  2006/03/04 23:03:45  cervelli
 * Added alias feature. More thoroughly incorporated calibrations.  Got rid of 'waves' tab and combined all functionality under a 'channels' tab.
 *
 * Revision 1.2  2005/10/01 15:46:01  dcervelli
 * A channel can now be in multiple groups.
 *
 * Revision 1.1  2005/08/26 20:40:28  dcervelli
 * Initial avosouth commit.
 *
 * Revision 1.4  2005/04/27 03:52:10  cervelli
 * Peter's configuration changes.
 *
 * Revision 1.3  2005/03/24 20:38:42  cervelli
 * Switched helicorder and wave tab order.
 *
 * Revision 1.2  2004/10/12 23:43:15  cvs
 * Added log info and some comments.
 *
 * @author Dan Cervelli
 */
public class ChannelPanel extends JPanel
{
	private static final long serialVersionUID = -1;
	private static final int MAX_CHANNELS_AT_ONCE = 500;
	private String source;
	private Swarm swarm;
//	private JScrollPane waveScrollPane;
	private JScrollPane heliScrollPane;
	private JButton realtimeButton;
	private JButton viewHeliButton;
//	private JPanel wavePanel;
	private JPanel heliPanel;
	
//	private JTree waveTree;
	private JTree heliTree;
	
	private ConfigFile groupFile;
	
	public ChannelPanel(Swarm sw)
	{
		this.setLayout(new BorderLayout());
		groupFile = new ConfigFile(Swarm.getParentFrame().getConfig().getString("groupConfigFile"));
		swarm = sw;
		SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						createUI();
					}
				});
	}

	public void createUI()
	{
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
	
		class makeVisibileTSL implements TreeSelectionListener
		{
			public void valueChanged(TreeSelectionEvent e) 
			{
				if (e.isAddedPath())
				{
					TreePath[] paths = e.getPaths();
					if (paths.length == 2)
						((JTree)e.getSource()).scrollPathToVisible(paths[0]);
				}
			}
		}
		heliPanel = new JPanel(new BorderLayout());
		
		JPanel heliButtonPanel = new JPanel(new GridLayout(1,4));
		
		rootNode = new DefaultMutableTreeNode("root");
		heliTree = new JTree(rootNode);
		heliTree.setRootVisible(false);
		heliTree.addMouseListener(new MouseAdapter()
				{
					public void mouseClicked(MouseEvent e)
					{
						if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
						{
							TreePath path = heliTree.getSelectionPath();
							if (path != null)
							{
								DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
								if (node != null && node.isLeaf())
									swarm.helicorderChannelSelected(source, node.toString());
							}
						}	
					}
				});
				
		heliTree.addKeyListener(new KeyAdapter()
				{
					public void keyTyped(KeyEvent e) 
					{
						if (e.getKeyChar() == 0x0a)
							viewHeliButton.doClick();
					}
				});

		heliTree.addTreeSelectionListener(new makeVisibileTSL());
		
		heliPanel = new JPanel(new BorderLayout());
		heliScrollPane = new JScrollPane(heliTree);				
		
//		viewHeliButton = new JButton("H");
		viewHeliButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(Images.get("heli"))));
		viewHeliButton.setToolTipText("Helicorder");
		viewHeliButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						TreePath[] paths = heliTree.getSelectionPaths();
						String[] channels = getSelectedLeaves(paths);
						for (int i = 0; i < channels.length; i++)
							swarm.helicorderChannelSelected(source, channels[i]);
					}
				});
		
		realtimeButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(Images.get("wave"))));
		realtimeButton.setToolTipText("Wave");
		realtimeButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						TreePath[] paths = heliTree.getSelectionPaths();
						String[] channels = getSelectedLeaves(paths);
						for (int i = 0; i < channels.length; i++)
							swarm.waveChannelSelected(source, channels[i]);
					}
				});
				
//		JButton clipboardButton = new JButton("C");
		JButton clipboardButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(Images.get("clipboard"))));
		clipboardButton.setToolTipText("Clipboard");
		clipboardButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ev)
					{
						TreePath[] paths = heliTree.getSelectionPaths();
						String[] channels = getSelectedLeaves(paths);
						if (channels != null)
							swarm.clipboardWaveChannelSelected(source, channels);
					}
				});
		
//		JButton monitorButton = new JButton("M");
		JButton monitorButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource(Images.get("monitor"))));
		monitorButton.setToolTipText("Monitor");
		monitorButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ev)
					{
						TreePath[] paths = heliTree.getSelectionPaths();
						String[] channels = getSelectedLeaves(paths);
						if (channels != null)
							swarm.monitorChannelSelected(source, channels);
					}
				});




		
		heliButtonPanel.add(viewHeliButton);
		heliButtonPanel.add(clipboardButton);
		heliButtonPanel.add(monitorButton);
		heliButtonPanel.add(realtimeButton);
		
		heliPanel.add(heliScrollPane, BorderLayout.CENTER);
		heliPanel.add(heliButtonPanel, BorderLayout.SOUTH);

		this.setBorder(new TitledBorder(new EtchedBorder(), "Data"));
		
		this.add(heliPanel, BorderLayout.CENTER);
//		this.add("Channels", heliPanel);
//		this.add("Waves", wavePanel);

	}
	
//	public void populateWaveChannels(final String s, final java.util.List items)
//	{
//		//populateTree(s, items, waveTree);
//	}
	
	public void populateHelicorderChannels(final String s, final java.util.List items)
	{
		populateTree(s, items, heliTree);
	}
	
	private String[] getSelectedLeaves(TreePath[] paths)
	{
		if (paths == null)
			return null;
			
		List<String> channels = new ArrayList<String>();
		for (int i = 0; i < paths.length; i++)
		{
			TreePath path = paths[i];
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			if (node.isLeaf())
			{
				if (!channels.contains(node.toString()))
					channels.add(node.toString());
			}
			else
			{
				for (Enumeration e = node.children() ; e.hasMoreElements() ;) 
				{
					DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)e.nextElement();
					if (node2.isLeaf())
						if (!channels.contains(node2.toString()))
							channels.add(node2.toString());
				}
			}
		}
		int cnt = channels.size();
		if (cnt > MAX_CHANNELS_AT_ONCE)
		{
			JOptionPane.showMessageDialog(Swarm.getParentFrame(), "You may only choose " + MAX_CHANNELS_AT_ONCE + " channels at one time.", "Error", JOptionPane.ERROR_MESSAGE);
			cnt = MAX_CHANNELS_AT_ONCE;
		}
		
		String[] sc = new String[cnt];
		for (int i = 0; i < cnt; i++)
			sc[i] = channels.get(i);
			
		return sc;
	}
	
	private void populateTree(final String s, final java.util.List items, final JTree tree)
	{
		if (items == null)
			return;
		
		SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						TreeMap<String, TreeNode> rootMap = new TreeMap<String, TreeNode>();
						source = s;
						DefaultMutableTreeNode allNode = new DefaultMutableTreeNode("All");
						DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
						rootNode.add(allNode);
						Iterator it = items.iterator();
						while (it.hasNext())
						{
							String c = it.next().toString();
							DefaultMutableTreeNode node = new DefaultMutableTreeNode(c);
							
							allNode.add(node);
							if (groupFile != null)
							{
								List<String> groups = groupFile.getList(c);
								if (groups != null)
								{
									for (String g : groups)
									{
										DefaultMutableTreeNode rn = (DefaultMutableTreeNode)rootMap.get(g);
										if (rn == null)
										{
											rn = new DefaultMutableTreeNode(g);
											rootMap.put(g, rn);
										}
										DefaultMutableTreeNode ln = new DefaultMutableTreeNode(c);
										rn.add(ln);
									}
								}
							}
						}
						
						for (it = rootMap.keySet().iterator(); it.hasNext();)
						{
							String k = (String)it.next();
							DefaultMutableTreeNode n = (DefaultMutableTreeNode)rootMap.get(k);
							rootNode.add(n);
						}
						((TitledBorder)getBorder()).setTitle(source);
						
						DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
						tree.setModel(treeModel);
						repaint();
					}
				});	
	}
}