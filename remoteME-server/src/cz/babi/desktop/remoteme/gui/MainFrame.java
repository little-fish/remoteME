/**
 * Copyright 2013 Martin Misiarz (dev.misiarz@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.babi.desktop.remoteme.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import cz.babi.desktop.remoteme.Settings;
import cz.babi.desktop.remoteme.common.Common;
import cz.babi.desktop.remoteme.connection.TCPClientHandler;
import cz.babi.desktop.remoteme.data.FileOperations;
import cz.babi.desktop.remoteme.data.Icons;
import cz.babi.desktop.remoteme.entity.MyThreadGroups;

/**
 * Main frame.
 * 
 * @author Martin Misiarz
 * @author dev.misiarz@gmail.com
 */
public class MainFrame extends JFrame implements Observer {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getSimpleName());
	private static Settings settings = Settings.getInstance();
	private final ResourceBundle l10n = Common.L10N;
	
	private JTabbedPane tabbedPane;
	
	private JPanel pnlTabServerInformation;
	private JPanel pnlServerInformation;
	private JPanel pnlTabAbout;
	private JPanel pnlPreferences;
	
	private JLabel lblIpAddress_;
	private JLabel lblMacAddress_;
	private JLabel lblActiveConnections_;
	private JLabel lblActiveConnections;
	private JLabel lblMacAddress;
	private JLabel lblIpAddress;
	
	private JPasswordField psfServerPassword;
	
	private JCheckBox chbActiveServer;
	private JCheckBox chbScanMode;
	private JCheckBox chbEncryptCommunication;
	private JCheckBox chbServerPassword;
	private JCheckBox chbConnectionPort;
	private JCheckBox chbScanPort;
	
	private JSpinner spnScanPort;
	private JSpinner spnConnectionPort;
	private JLabel lblHomePageLink;
	private JLabel lblEmail;
	private JLabel lblLicenseText;
	private JLabel lblApacheLink;
	private JPanel pnlLicense;
	private JLabel lblCopyright;
	private JPanel pnlSupport;
	private JLabel lblSupportText;
	
	private JButton btnDonate;
	
	/**
	 * Constructor.
	 */
	public MainFrame() {
		initGUI();
		
		/* Add observer. */
		if(MyThreadGroups.getInstance().getTcpServer()!=null)
			MyThreadGroups.getInstance().getTcpServer().addObserver(this);
	}
	
	/**
	 * Init GUI components.
	 */
	private void initGUI() {
		if(Common.DEBUG)
			LOGGER.debug("[initGUI]");
		
		String appName = l10n.getString("Application.name");
		String appVersion = l10n.getString("Application.version");
		
		setTitle(appName + " v" + appVersion);
		setSize(506, 434);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(Icons.APP_ICON.getImage());
		
		tabbedPane = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		pnlTabServerInformation = new JPanel();
		tabbedPane.addTab(
				l10n.getString("MainFrame.tab.serverinformation.title.text"),
				null, pnlTabServerInformation,
				l10n.getString("MainFrame.tab.serverinformation.tooltip.text"));
		
		pnlServerInformation = new JPanel();
		pnlServerInformation
		.setBorder(new TitledBorder(
				null,
				l10n.getString("MainFrame.tab.serverinformation.pnl.status.title.text"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		lblIpAddress_ = new JLabel(
				l10n.getString("MainFrame.tab.serverinformation.lbl.ipaddress.text"));
		
		lblMacAddress_ = new JLabel(
				l10n.getString("MainFrame.tab.serverinformation.lbl.macaddress.text"));
		
		lblActiveConnections_ = new JLabel(
				l10n.getString("MainFrame.tab.serverinformation.lbl.activeconnection.text"));
		
		String activeConnections;
		if(MyThreadGroups.getInstance().getTcpServer()!=null)
			activeConnections = String.valueOf(MyThreadGroups.getInstance().getTcpServer().getClients().size());
		else activeConnections = "0";
		lblActiveConnections = new JLabel(activeConnections);
		lblActiveConnections.setToolTipText(l10n.getString("MainFrame.tab.serverinformation.lbl.activeconnection.tooltip"));
		lblActiveConnections.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		String macAddres = Common.getMapAddress();
		if(macAddres==null) macAddres = l10n.getString("MainFrame.tab.serverinformation.lbl.macaddress.unknown.text");
		lblMacAddress = new JLabel(macAddres);
		lblMacAddress.setToolTipText(l10n.getString("MainFrame.tab.serverinformation.lbl.macaddress.tooltip"));
		lblMacAddress.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		String ipAddress = Common.getLocalIpAddress();
		if(ipAddress==null) ipAddress = l10n.getString("MainFrame.tab.serverinformation.lbl.ipaddress.unknown.text");
		lblIpAddress = new JLabel(ipAddress);
		lblIpAddress.setToolTipText(l10n.getString("MainFrame.tab.serverinformation.lbl.ipaddress.tooltip"));
		lblIpAddress.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		GroupLayout gl_pnlServerInformation = new GroupLayout(
				pnlServerInformation);
		gl_pnlServerInformation.setHorizontalGroup(
				gl_pnlServerInformation.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlServerInformation.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlServerInformation.createParallelGroup(Alignment.LEADING)
								.addComponent(lblMacAddress_)
								.addComponent(lblIpAddress_)
								.addComponent(lblActiveConnections_))
								.addGap(18)
								.addGroup(gl_pnlServerInformation.createParallelGroup(Alignment.LEADING)
										.addComponent(lblIpAddress)
										.addComponent(lblMacAddress)
										.addComponent(lblActiveConnections))
										.addContainerGap(189, Short.MAX_VALUE))
				);
		gl_pnlServerInformation.setVerticalGroup(
				gl_pnlServerInformation.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlServerInformation.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlServerInformation.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblIpAddress_)
								.addComponent(lblIpAddress))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlServerInformation.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblMacAddress_)
										.addComponent(lblMacAddress))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_pnlServerInformation.createParallelGroup(Alignment.BASELINE)
												.addComponent(lblActiveConnections)
												.addComponent(lblActiveConnections_))
												.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlServerInformation.setLayout(gl_pnlServerInformation);
		
		pnlPreferences = new JPanel();
		pnlPreferences
		.setBorder(new TitledBorder(
				null,
				l10n.getString("MainFrame.tab.serverinformation.pnl.preferences.title.text"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout gl_pnlTabServerInformation = new GroupLayout(
				pnlTabServerInformation);
		gl_pnlTabServerInformation.setHorizontalGroup(
				gl_pnlTabServerInformation.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlTabServerInformation.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlTabServerInformation.createParallelGroup(Alignment.TRAILING)
								.addComponent(pnlPreferences, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
								.addComponent(pnlServerInformation, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE))
								.addContainerGap())
				);
		gl_pnlTabServerInformation.setVerticalGroup(
				gl_pnlTabServerInformation.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlTabServerInformation.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlServerInformation, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlPreferences, GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
						.addContainerGap())
				);
		
		chbActiveServer = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.activeserver.text"),
				settings.isVisibleServer());
		chbActiveServer.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbActiveServerItemStateChanged(e);
			}
		});
		
		chbScanMode = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.scanmode.text"),
				settings.isDiscoverableServer());
		chbScanMode.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbScanModeItemStateChanged(e);
			}
		});
		
		chbEncryptCommunication = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.encryptcommunication.text"),
				settings.isEncryptedCommunication());
		chbEncryptCommunication.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbEncryptCommunicationitemStateChanged(e);
			}
		});
		
		chbServerPassword = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.serverpassword.text"),
				settings.isProtectWithPassword());
		chbServerPassword.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbServerPasswordItemStateChanged(e);
			}
		});
		
		psfServerPassword = new JPasswordField(String.valueOf(settings.getUserPassword()));
		psfServerPassword.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				psfServerPasswordCaretUpdate(e);
			}
		});
		psfServerPassword.setEnabled(settings.isProtectWithPassword());
		
		JSeparator separator = new JSeparator();
		
		chbConnectionPort = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.connectionport.text"),
				settings.isCustomConnectionPort());
		chbConnectionPort.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbConnectionPortItemStateChanged(e);
			}
		});
		
		chbScanPort = new JCheckBox(l10n.getString("MainFrame.tab.serverinformation.chb.scanmodeport.text"),
				settings.isCustomScanPort());
		chbScanPort.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chbScanPortItemStateChanged(e);
			}
		});
		
		spnScanPort = new JSpinner();
		spnScanPort.setFont(new Font("Dialog", Font.PLAIN, 12));
		spnScanPort.setEnabled(settings.isCustomScanPort());
		spnScanPort.setModel(new SpinnerNumberModel(settings.getScanPort(), 1024, 65535, 1));
		spnScanPort.setEditor(new JSpinner.NumberEditor(spnScanPort,"#"));
		spnScanPort.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				spnScanPortStateChanged(e);
			}
		});
		
		spnConnectionPort = new JSpinner();
		spnConnectionPort.setFont(new Font("Dialog", Font.PLAIN, 12));
		spnConnectionPort.setEnabled(settings.isCustomConnectionPort());
		spnConnectionPort.setModel(new SpinnerNumberModel(4449, 1024, 65535, 1));
		spnConnectionPort.setEditor(new JSpinner.NumberEditor(spnConnectionPort,"#"));
		spnConnectionPort.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				spnConnectionPortStateChanged(e);
			}
		});
		
		GroupLayout gl_pnlPreferences = new GroupLayout(pnlPreferences);
		gl_pnlPreferences.setHorizontalGroup(
				gl_pnlPreferences.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlPreferences.createSequentialGroup()
						.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlPreferences.createSequentialGroup()
										.addGap(29)
										.addComponent(psfServerPassword, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE))
										.addGroup(gl_pnlPreferences.createSequentialGroup()
												.addContainerGap()
												.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.LEADING)
														.addComponent(chbActiveServer)
														.addComponent(chbScanMode)
														.addComponent(chbEncryptCommunication)
														.addComponent(chbServerPassword)
														.addComponent(separator, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
														.addGroup(gl_pnlPreferences.createSequentialGroup()
																.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.LEADING)
																		.addComponent(chbScanPort)
																		.addComponent(chbConnectionPort))
																		.addPreferredGap(ComponentPlacement.UNRELATED, 12, Short.MAX_VALUE)
																		.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.TRAILING, false)
																				.addComponent(spnScanPort)
																				.addComponent(spnConnectionPort))))))
																				.addContainerGap())
				);
		gl_pnlPreferences.setVerticalGroup(
				gl_pnlPreferences.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlPreferences.createSequentialGroup()
						.addContainerGap()
						.addComponent(chbActiveServer)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chbScanMode)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chbEncryptCommunication)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chbServerPassword)
						.addGap(8)
						.addComponent(psfServerPassword, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.BASELINE)
								.addComponent(chbConnectionPort)
								.addComponent(spnConnectionPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlPreferences.createParallelGroup(Alignment.BASELINE)
										.addComponent(chbScanPort)
										.addComponent(spnScanPort, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE))
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlPreferences.setLayout(gl_pnlPreferences);
		pnlTabServerInformation.setLayout(gl_pnlTabServerInformation);
		
		pnlTabAbout = new JPanel();
		tabbedPane.addTab(l10n.getString("MainFrame.tab.about.title.text"),
				null, pnlTabAbout,
				l10n.getString("MainFrame.tab.about.tooltip.text"));
		
		JPanel pnlHomePage = new JPanel();
		pnlHomePage.setBorder(new TitledBorder(null, l10n.getString("MainFrame.tab.about.lbl.homepage.title.text"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		lblHomePageLink = new JLabel(l10n.getString("MainFrame.tab.about.lbl.homepage.html"));
		lblHomePageLink.setForeground(Color.BLUE);
		lblHomePageLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblHomePageLinkMouseClicked(e);
			}
		});
		lblHomePageLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblHomePageLink.setFont(new Font("Dialog", Font.PLAIN, 12));
		GroupLayout gl_pnlHomePage = new GroupLayout(pnlHomePage);
		gl_pnlHomePage.setHorizontalGroup(
				gl_pnlHomePage.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlHomePage.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblHomePageLink)
						.addContainerGap(170, Short.MAX_VALUE))
				);
		gl_pnlHomePage.setVerticalGroup(
				gl_pnlHomePage.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_pnlHomePage.createSequentialGroup()
						.addContainerGap(25, Short.MAX_VALUE)
						.addComponent(lblHomePageLink)
						.addContainerGap())
				);
		pnlHomePage.setLayout(gl_pnlHomePage);
		
		pnlLicense = new JPanel();
		pnlLicense.setBorder(new TitledBorder(null, l10n.getString("MainFrame.tab.about.lbl.license.title.text"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		lblEmail = new JLabel(l10n.getString("MainFrame.tab.about.lbl.copyright.email"));
		lblEmail.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblEmailMouseClicked(e);
			}
		});
		lblEmail.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblEmail.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblEmail.setForeground(Color.BLUE);
		
		lblLicenseText = new JLabel(l10n.getString("MainFrame.tab.about.lbl.license.text"));
		lblLicenseText.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		lblApacheLink = new JLabel(l10n.getString("MainFrame.tab.about.lbl.license.apache.text"));
		lblApacheLink.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				lblApacheLinkMouseClicked(e);
			}
		});
		lblApacheLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lblApacheLink.setForeground(Color.BLUE);
		lblApacheLink.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		lblCopyright = new JLabel(l10n.getString("MainFrame.tab.about.lbl.copyright.text"));
		lblCopyright.setMaximumSize(new Dimension(158, 15));
		lblCopyright.setFont(new Font("Dialog", Font.PLAIN, 12));
		GroupLayout gl_pnlLicense = new GroupLayout(pnlLicense);
		gl_pnlLicense.setHorizontalGroup(
				gl_pnlLicense.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlLicense.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlLicense.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlLicense.createSequentialGroup()
										.addComponent(lblLicenseText)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblApacheLink))
										.addGroup(gl_pnlLicense.createSequentialGroup()
												.addComponent(lblCopyright, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(lblEmail)))
												.addContainerGap(69, Short.MAX_VALUE))
				);
		gl_pnlLicense.setVerticalGroup(
				gl_pnlLicense.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlLicense.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlLicense.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblCopyright, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblEmail))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlLicense.createParallelGroup(Alignment.BASELINE)
										.addComponent(lblLicenseText)
										.addComponent(lblApacheLink))
										.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlLicense.setLayout(gl_pnlLicense);
		
		pnlSupport = new JPanel();
		pnlSupport.setBorder(new TitledBorder(null, l10n.getString("MainFrame.tab.about.lbl.support.title.text"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		lblSupportText = new JLabel(l10n.getString("MainFrame.tab.about.lbl.support.text"));
		lblSupportText.setFont(new Font("Dialog", Font.PLAIN, 12));
		
		btnDonate = new JButton(Icons.PAYPAL_DONATE);
		btnDonate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				btnDonateActionPerformed(e);
			}
		});
		btnDonate.setMargin(new Insets(0, 0, 0, 0));
		btnDonate.setIconTextGap(0);
		GroupLayout gl_pnlSupport = new GroupLayout(pnlSupport);
		gl_pnlSupport.setHorizontalGroup(
				gl_pnlSupport.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlSupport.createSequentialGroup()
						.addGroup(gl_pnlSupport.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlSupport.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblSupportText))
										.addGroup(gl_pnlSupport.createSequentialGroup()
												.addGap(149)
												.addComponent(btnDonate)))
												.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		gl_pnlSupport.setVerticalGroup(
				gl_pnlSupport.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlSupport.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblSupportText)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btnDonate)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlSupport.setLayout(gl_pnlSupport);
		GroupLayout gl_pnlTabAbout = new GroupLayout(pnlTabAbout);
		gl_pnlTabAbout.setHorizontalGroup(
				gl_pnlTabAbout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_pnlTabAbout.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlTabAbout.createParallelGroup(Alignment.TRAILING)
								.addComponent(pnlSupport, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
								.addComponent(pnlHomePage, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
								.addComponent(pnlLicense, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE))
								.addContainerGap())
				);
		gl_pnlTabAbout.setVerticalGroup(
				gl_pnlTabAbout.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlTabAbout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlHomePage, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlLicense, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlSupport, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(46, Short.MAX_VALUE))
				);
		pnlTabAbout.setLayout(gl_pnlTabAbout);
	}
	
	/**
	 * Show main frame.
	 */
	public void showFrame() {
		if(Common.DEBUG)
			LOGGER.debug("[showFrame]");
		
		setVisible(true);
	}
	
	/**
	 * Call after user change state of TCP server.
	 * @param itemEvent ItemEvent.
	 */
	private void chbActiveServerItemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setVisibleServer(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		/* Start or stop server(s). */
		if(isSelected) {
			int port;
			
			if(settings.isCustomConnectionPort()) port = settings.getConnectionPort();
			else port = Common.DEFAULT_PORT;
			
			MyThreadGroups.getInstance().createTCPServer(port);
			/* Add observer. */
			MyThreadGroups.getInstance().getTcpServer().addObserver(this);
		} else {
			disconnectClients();
			/* Stop server. */
			MyThreadGroups.getInstance().getTcpServer().stopServer();
		}
	}
	
	/**
	 * Call after user change state of UDP scan mode.
	 * @param itemEvent ItemEvent.
	 */
	private void chbScanModeItemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setDiscoverableServer(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		/* Start or stop server(s). */
		if(isSelected) {
			int port;
			
			if(settings.isCustomScanPort()) port = settings.getScanPort();
			else port = Common.DEFAULT_PORT;
			
			MyThreadGroups.getInstance().createUDPServer(port);
		}
		else {
			/* Stop server. */
			MyThreadGroups.getInstance().getUdpScanModeServer().stopServer();
		}
	}
	
	/**
	 * Call after user change state of Encrypted communication.
	 * @param itemEvent ItemEvent.
	 */
	private void chbEncryptCommunicationitemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setEncryptedCommunication(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		/* Close connection for all clients to apply new settings. */
		disconnectClients();
	}
	
	/**
	 * Call after user change state of Password protection.
	 * @param itemEvent ItemEvent.
	 */
	private void chbServerPasswordItemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setProtectWithPassword(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		/* Close connection for all clients to apply new settings. */
		disconnectClients();
		/* Enable/disable password field. */
		if(isSelected) psfServerPassword.setEnabled(true);
		else psfServerPassword.setEnabled(false);
	}
	
	/**
	 * Call after user change password.
	 * @param caretEvent CaretEvent.
	 */
	private void psfServerPasswordCaretUpdate(CaretEvent caretEvent) {
		/* Obtain typed password. */
		String currentPassword = new String(psfServerPassword.getPassword());
		/* Save current password. */
		settings.setUserPassword(currentPassword);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		/* Close connection for all clients to apply new settings. */
		disconnectClients();
	}
	
	/**
	 * Call after user change state of custom connection port.
	 * @param itemEvent ItemEvent.
	 */
	private void chbConnectionPortItemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setCustomConnectionPort(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		
		if(isSelected) {
			spnConnectionPort.setEnabled(true);
			
			disconnectClients();
			
			/* Stop server. */
			MyThreadGroups.getInstance().getTcpServer().stopServer();
			/* Start server with USER port */
			MyThreadGroups.getInstance().createTCPServer(settings.getConnectionPort());
			/* Add observer. */
			MyThreadGroups.getInstance().getTcpServer().addObserver(this);
		} else {
			spnConnectionPort.setEnabled(false);
			
			disconnectClients();
			
			/* Stop server. */
			MyThreadGroups.getInstance().getTcpServer().stopServer();
			/* Start server with DEFAULT port */
			MyThreadGroups.getInstance().createTCPServer(Common.DEFAULT_PORT);
			/* Add observer. */
			MyThreadGroups.getInstance().getTcpServer().addObserver(this);
		}
		/* Set connecton port. */
		spnConnectionPort.setValue(settings.getConnectionPort());
	}
	
	/**
	 * Call after user change state of custom scan mode port.
	 * @param itemEvent ItemEvent.
	 */
	private void chbScanPortItemStateChanged(ItemEvent itemEvent) {
		/* Obtain state. */
		boolean isSelected = itemEvent.getStateChange()==ItemEvent.SELECTED;
		/* Save state. */
		settings.setCustomScanPort(isSelected);
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		
		if(isSelected) {
			spnScanPort.setEnabled(true);
			/* Stop server. */
			MyThreadGroups.getInstance().getUdpScanModeServer().stopServer();
			/* Create server with USER port. */
			MyThreadGroups.getInstance().createUDPServer(settings.getScanPort());
		} else {
			spnScanPort.setEnabled(false);
			/* Stop server. */
			MyThreadGroups.getInstance().getUdpScanModeServer().stopServer();
			/* Create server with DEFAULT port. */
			MyThreadGroups.getInstance().createUDPServer(Common.DEFAULT_PORT);
		}
		/* Set scan port. */
		spnScanPort.setValue(settings.getScanPort());
	}
	
	/**
	 * Call after user change scan port.
	 * @param changeEvent ChangeEvent.
	 */
	private void spnScanPortStateChanged(ChangeEvent changeEvent) {
		JSpinner source = (JSpinner)changeEvent.getSource();
		
		settings.setScanPort((Integer)source.getValue());
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		
		/* Stop server. */
		MyThreadGroups.getInstance().getUdpScanModeServer().stopServer();
		/* Start server with new port */
		MyThreadGroups.getInstance().createUDPServer(settings.getScanPort());
	}
	
	/**
	 * Call after user change connection port.
	 * @param changeEvent ChangeEvent.
	 */
	private void spnConnectionPortStateChanged(ChangeEvent changeEvent) {
		JSpinner source = (JSpinner)changeEvent.getSource();
		
		settings.setConnectionPort((Integer)source.getValue());
		/* Save settings. */
		FileOperations.getInstance().needSaveSettings();
		
		disconnectClients();
		
		/* Stop server. */
		MyThreadGroups.getInstance().getTcpServer().stopServer();
		/* Start server with new port */
		MyThreadGroups.getInstance().createTCPServer(settings.getConnectionPort());
		/* Add observer. */
		MyThreadGroups.getInstance().getTcpServer().addObserver(this);
	}
	
	/**
	 * Disconnect all clients.
	 */
	private void disconnectClients() {
		if(MyThreadGroups.getInstance().getTcpServer()!=null) {
			/* Close connection for all clients. */
			for(TCPClientHandler client : MyThreadGroups.getInstance().getTcpServer().getClients())
				client.forceDisconnect();
			MyThreadGroups.getInstance().getTcpServer().clearClients();
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		/* If there is new client we need to show it to user. */
		lblActiveConnections.setText(String.valueOf(arg));
	}
	
	/**
	 * Call after user click on homepage label.
	 * @param mouseEvent MouseEvent.
	 */
	private void lblHomePageLinkMouseClicked(MouseEvent mouseEvent) {
		openWebPage(l10n.getString("MainFrame.tab.about.lbl.homepage.html"));
	}
	
	/**
	 * Create new email.
	 * @param mouseEvent MouseEvent
	 */
	private void lblEmailMouseClicked(MouseEvent mouseEvent) {
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().mail(new URI("mailto:" + l10n.getString("MainFrame.tab.about.lbl.copyright.email")));
			} catch(URISyntaxException urise) {
				if(Common.ERROR) LOGGER.error("[Can not resolve internet address.]", urise);
			} catch(IOException ioe) {
				if(Common.ERROR) LOGGER.error("[Can not open web browser.]", ioe);
			}
		}
	}
	
	/**
	 * Call after user click on licese label
	 * @param mouseEvent
	 */
	private void lblApacheLinkMouseClicked(MouseEvent mouseEvent) {
		openWebPage(l10n.getString("MainFrame.tab.about.lbl.license.apache.html"));
	}
	
	/**
	 * Call after user click on donate button.
	 * @param actionEvent
	 */
	private void btnDonateActionPerformed(ActionEvent actionEvent) {
		openWebPage(l10n.getString("MainFrame.tab.about.lbl.support.paypal.html"));
	}
	
	/**
	 * Open web page.
	 * @param uri URL.
	 */
	private void openWebPage(String uri) {
		if(Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(uri));
			} catch(URISyntaxException urise) {
				if(Common.ERROR) LOGGER.error("[Can not resolve internet address.]", urise);
			} catch(IOException ioe) {
				if(Common.ERROR) LOGGER.error("[Can not open web browser.]", ioe);
			}
		}
	}
}
