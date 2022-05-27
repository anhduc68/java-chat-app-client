package tcp.client;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.ObjectInputStream;
import java.lang.ref.Cleaner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import model.Group;
import model.GroupMsg;
import model.Message;
import model.ObjectWrapper;
import model.User;
import rmi_controller.IUser;

public class HomeFrm extends javax.swing.JFrame {

    private ClientCtr client;
    private Thread listener;
    private DefaultTableModel tablePlayer, tableFrRequest, tableFriend, tableListFriend;
    private String res;
    private IUser  i = null;

    public HomeFrm(ClientCtr client) {
        initComponents();
        this.setLocationRelativeTo(this);
        this.client = client;
        new Thread(new Listener()).start();
        tableFriend = (DefaultTableModel) tblFriend.getModel();
        tableFrRequest = (DefaultTableModel) tblFriendRequest.getModel();
        tableListFriend = (DefaultTableModel) tblListFriend.getModel();
        showFriendRequest();
        showListFriend();

        //set LableUser
        lbUserMain.setText(client.getUser().getUsername());
        lbUser1.setText("User: " + client.getUser().getUsername());
        lbUser2.setText("User: " + client.getUser().getUsername());
        lbUser3.setText("User: " + client.getUser().getUsername());
        lbUser4.setText("User: " + client.getUser().getUsername());
        lbUser5.setText("User: " + client.getUser().getUsername());
        
        //set IUser
            try {
                this.i = (IUser) java.rmi.Naming.lookup("rmi://" + "127.0.0.1" + ":1099/user");
            }
            catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không thể kết nối tới máy chủ RMI");
            }
        // close window
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                handleClose();
            }
        });
        

    }

    private HomeFrm() {
    }

    private void setUser(User user) {
        this.client.setUser(user);
        txtEmail.setText(user.getEmail());
        txtUsername.setText(user.getUsername());
        txtPass.setText(user.getPassword());
        txtCfPass.setText(user.getPassword());
        setImageUser();
    }

    private void setImageUser() {
        IUser i = null;
        try {
            i = (IUser) java.rmi.Naming.lookup("rmi://" + "127.0.0.1" + ":1099/user");
            byte[] b = i.retriveImg(this.client.getUser());
            if (b != null) {
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(b).getImage().getScaledInstance(lbImage.getWidth(), lbImage.getHeight(), Image.SCALE_DEFAULT));
                lbImage.setIcon(imageIcon);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối tới máy chủ RMI");
        }
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg);
    }

    private void getNotify(String msg) {
        txtThongBao.setText(txtThongBao.getText() + msg + "\n");
        showMessage(msg);

    }

    public void handleClose() {
        client.sendData(new ObjectWrapper(ObjectWrapper.QUIT, "quit"));
        client.disconnect();

    }

    public void UpdateChatTextPanel(String msg) {
        ChatTextPanel.setText(ChatTextPanel.getText() + msg + "\n");
    }

    public void UpdateChatTextGroupPanel(String msg) {
        ChatTextGroupPanel.setText(ChatTextGroupPanel.getText() + msg + "\n");

    }

    public void resetChatTextPanel() {
        ChatTextPanel.setText("");
    }

    // thread
    class Listener implements Runnable {

        public Listener() {
        }

        public void run() {
            try {

                while (true) {
                    ObjectInputStream ois = new ObjectInputStream(client.getSocket().getInputStream());
                    //get Object 
                    Object o = ois.readObject();
                    if (o instanceof ObjectWrapper) {
                        ObjectWrapper data = (ObjectWrapper) o;
                        switch (data.getPerformative()) {
                            case ObjectWrapper.REPLY_GET_USER:
                                User user = (User) data.getData();
                                setUser(user);
                                break;
                            case ObjectWrapper.NOTIFY_TO_USER:
                                res = (String) data.getData();
                                getNotify(res);
                                break;
                            case ObjectWrapper.REPLY_SEARCH_USER:
                                ArrayList<User> list = (ArrayList<User>) data.getData();
                                //
                                if (list.size() == 0) {
                                    JOptionPane.showMessageDialog(null, "no result");
                                    tableFriend.setRowCount(0);
                                } else {
                                    tableFriend.setRowCount(0);
                                    for (User u : list) {
                                        tableFriend.addRow(new Object[]{
                                            u.getId(), u.getUsername(), u.getEmail()
                                        });
                                    }
                                }
                                break;
                            case ObjectWrapper.REPLY_ADDFRIEND_REQUEST:
                                res = (String) data.getData();
                                if (res.equals("ok")) {
                                    JOptionPane.showMessageDialog(null, "Gui loi moi ket ban thanh cong");
                                    int row = tblFriend.getSelectedRow();
                                    tableFriend.removeRow(row);
                                } else if (res.equals("exist")) {
                                    JOptionPane.showMessageDialog(null, "Ban da gui loi moi toi nguoi nay roi!");
                                } else {
                                    showMessage("Ko the gui loi moi!");
                                }
                                break;
                            case ObjectWrapper.REPLY_SHOW_FRIEND_REQUEST:
                                ArrayList<User> list_user_rq = (ArrayList<User>) data.getData();
                                tableFrRequest.setRowCount(0);
                                if (list_user_rq.size() == 0) {
                                    // showMessage("Khong tim thay loi moi ket ban nao!");
                                } else {
                                    for (User u : list_user_rq) {
                                        tableFrRequest.addRow(new Object[]{
                                            u.getId(), u.getUsername(), u.getEmail()
                                        });
                                    }
                                }
                                break;
                            case ObjectWrapper.REPLY_ADD_FRIEND:
                                res = (String) data.getData();
                                if (res.equals("ok")) {
                                    JOptionPane.showMessageDialog(null, "You have been  accpect a request sucessfully!");
                                    showFriendRequest();
                                    tableFriend.setRowCount(0);
                                } else if (res.equals("exist")) {
                                    showMessage("Hai ban da tro thanh ban be roi!");
                                } else {
                                    JOptionPane.showMessageDialog(null, "Loi xac nhan");
                                }
                                break;
                            case ObjectWrapper.REPLY_SHOW_LIST_FRIEND:
                                ArrayList<User> listFriend = (ArrayList<User>) data.getData();
                                tableListFriend.setRowCount(0);
                                if (listFriend.size() == 0) {
                                    //  showMessage("Ban chua co nguoi ban nao!");
                                } else {
                                    for (User u : listFriend) {
                                        tableListFriend.addRow(new Object[]{
                                            u.getId(), u.getUsername(), u.getEmail()
                                        });
                                    }
                                }
                                break;
                            case ObjectWrapper.LIST_ONLINE_FRIEND:
                                ArrayList<User> listOnline = (ArrayList<User>) data.getData();
                                String userlist = "";
                                for (int i = 0; i < listOnline.size(); i++) {
                                    userlist += listOnline.get(i).getUsername() + " ";
                                }
                                String[] username = userlist.split(" ");
                                ListOnlineFriend.setListData(username);
                                resetChatTextPanel();
                                break;

                            // khi co nguoi nhan tin den, chon dung dong nguoi do nhan tren JList
                            case ObjectWrapper.SELECT_USER_MESSAGE_TO:
                                String username_msg_to = (String) data.getData();
                                ListModel listModel = ListOnlineFriend.getModel();
                                for (int i = 0; i < listModel.getSize(); i++) {
                                    if (listModel.getElementAt(i).toString().equals(username_msg_to)) {
                                        ListOnlineFriend.setSelectedIndex(i);
                                        break;
                                    }
                                }
                                break;
                            // cap nhat tin nhan len JList
                            case ObjectWrapper.REPLY_SHOW_LIST_MESSAGE:
                                Object obj = (Object) data.getData();
                                if (obj instanceof String) {
                                    showMessage("Khong the load tin nhan!");
                                } else {
                                    //clear
                                    ChatTextPanel.setText("");
                                    ArrayList<Message> listMsg = (ArrayList<Message>) data.getData();
                                    for (Message msg : listMsg) {
                                        if (msg.getFrom_id() == client.getUser().getId()) {
                                            String tinNhan = client.getUser().getUsername() + ": " + msg.getMsg();
                                            UpdateChatTextPanel(tinNhan);
                                        } else {
                                            String tinNhan = (String) ListOnlineFriend.getSelectedValue() + ": " + msg.getMsg();
                                            UpdateChatTextPanel(tinNhan);
                                        }
                                    }
                                }
                                break;
                            case ObjectWrapper.UPDATE_NEWS:
                                res = (String) data.getData();
                                NewsPanel.setText(NewsPanel.getText() + res + "\n" + "\n");
                                break;
                            case ObjectWrapper.REPLY_ADD_GROUP:
                                res = (String) data.getData();
                                if (res.equals("ok")) {
                                    showMessage("Create Group Sucessfully!");

                                } else if (res.equals("exist")) {
                                    showMessage("Ten group da ton tai!");
                                } else {
                                    showMessage("Loi tao group");
                                }
                                break;
                            case ObjectWrapper.REPLY_JOIN_GROUP:
                                res = (String) data.getData();
                                if (res.equals("GroupNoExist")) {
                                    showMessage("Khong tim thay group!");
                                } else if (res.equals("JoinExist")) {
                                    showMessage("Ban da gia nap group nay roi!");
                                } else if (res.equals("ok")) {
                                    showMessage("Join Group sucessfully!");
                                } else {
                                    showMessage("Loi Join Group!");
                                }
                                break;
                            case ObjectWrapper.REPLY_SHOW_LIST_GROUP:
                                ArrayList<Group> listG = (ArrayList<Group>) data.getData();
                                String listGroup = "";
                                for (int i = 0; i < listG.size(); i++) {
                                    listGroup += listG.get(i).getGr_name() + " ";
                                }
                                String[] group = listGroup.split(" ");
                                ListGroup.setListData(group);
                                ChatTextGroupPanel.setText("");
                                break;
                            case ObjectWrapper.REPLY_SHOW_LIST_GROUP_MESSAGE:
                                Object obj1 = (Object) data.getData();
                                if (obj1 instanceof String) {
                                    showMessage("Khong the load tin nhan!");
                                } else {
                                    //clear
                                    ChatTextGroupPanel.setText("");
                                    // get listMsg
                                    ArrayList<GroupMsg> listMsg = (ArrayList<GroupMsg>) data.getData();
                                    // load msg
                                    for (GroupMsg msg : listMsg) {
                                        String tinNhan = msg.getMsg();
                                        UpdateChatTextGroupPanel(tinNhan);
                                    }
                                }
                                break;
                            case ObjectWrapper.SELECT_GROURP_MESSAGE_TO:
                                String gr_name = (String) data.getData();
                                ListModel listModelGroup = ListGroup.getModel();
                                for (int i = 0; i < listModelGroup.getSize(); i++) {
                                    if (listModelGroup.getElementAt(i).toString().equals(gr_name)) {
                                        ListGroup.setSelectedIndex(i);
                                        break;
                                    }
                                }
                                break;
                        } //end of case
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        NewsPanel = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        lbUserMain = new javax.swing.JLabel();
        txtUpdateStatus = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtKey = new javax.swing.JTextField();
        btnSearchPlayerByName = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        tblFriend = new javax.swing.JTable();
        btnViewProfile = new javax.swing.JButton();
        lbUser1 = new javax.swing.JLabel();
        btnAddFriend = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        showRequest = new javax.swing.JButton();
        btnAccept = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblFriendRequest = new javax.swing.JTable();
        lbUser2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblListFriend = new javax.swing.JTable();
        btnResetFriendList = new javax.swing.JButton();
        lbUser3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        userListPane = new javax.swing.JScrollPane();
        ListOnlineFriend = new javax.swing.JList();
        lbMsg = new javax.swing.JLabel();
        lbUser4 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        ChatTextPanel = new javax.swing.JTextPane();
        txtMsg = new javax.swing.JTextField();
        btnSendMsg = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        userListPane1 = new javax.swing.JScrollPane();
        ListGroup = new javax.swing.JList();
        jScrollPane8 = new javax.swing.JScrollPane();
        ChatTextGroupPanel = new javax.swing.JTextPane();
        lbUser5 = new javax.swing.JLabel();
        lbMsg1 = new javax.swing.JLabel();
        txtMsgGroup = new javax.swing.JTextField();
        btnSendMsgGroup = new javax.swing.JButton();
        btnCreatGroup = new javax.swing.JButton();
        btnJoinGroup = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        txtThongBao = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        lbImage = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        txtAvt = new javax.swing.JButton();
        txtCfPass = new javax.swing.JPasswordField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtPass = new javax.swing.JPasswordField();

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.setAlignmentX(0.0F);
        jTabbedPane1.setAlignmentY(0.0F);

        NewsPanel.setEditable(false);
        NewsPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        NewsPanel.setFont(new java.awt.Font("Tekton Pro", 0, 15)); // NOI18N
        jScrollPane1.setViewportView(NewsPanel);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Chat Application");

        lbUserMain.setFont(new java.awt.Font("Tekton Pro", 0, 16)); // NOI18N
        lbUserMain.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbUserMain.setText(" Duc");

        txtUpdateStatus.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtUpdateStatus.setText("  What's in your mine?");
        txtUpdateStatus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtUpdateStatusKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 777, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lbUserMain, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUpdateStatus))
                            .addComponent(jScrollPane1))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtUpdateStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbUserMain, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("  Home", jPanel1);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Username:");

        txtKey.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        btnSearchPlayerByName.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnSearchPlayerByName.setText("search");
        btnSearchPlayerByName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchPlayerByNameActionPerformed(evt);
            }
        });

        tblFriend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblFriend.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Username", "Email"
            }
        ));
        jScrollPane3.setViewportView(tblFriend);

        btnViewProfile.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnViewProfile.setText("Xem trang cá nhân");
        btnViewProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnViewProfileActionPerformed(evt);
            }
        });

        lbUser1.setFont(new java.awt.Font("Tekton Pro", 0, 24)); // NOI18N
        lbUser1.setText("User: ");

        btnAddFriend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnAddFriend.setText("Ket ban");
        btnAddFriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFriendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btnViewProfile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lbUser1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(txtKey, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(btnSearchPlayerByName))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 734, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 21, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addContainerGap(651, Short.MAX_VALUE)
                    .addComponent(btnAddFriend)
                    .addGap(55, 55, 55)))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(txtKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnSearchPlayerByName, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(lbUser1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(btnViewProfile)))
                .addGap(20, 20, 20))
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addContainerGap(358, Short.MAX_VALUE)
                    .addComponent(btnAddFriend)
                    .addGap(67, 67, 67)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Kết bạn", jPanel3);

        showRequest.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        showRequest.setText("Refresh");
        showRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRequestActionPerformed(evt);
            }
        });

        btnAccept.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnAccept.setText("Accept");
        btnAccept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAcceptActionPerformed(evt);
            }
        });

        tblFriendRequest.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblFriendRequest.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Username", "Email"
            }
        ));
        jScrollPane2.setViewportView(tblFriendRequest);

        lbUser2.setFont(new java.awt.Font("Tekton Pro", 0, 24)); // NOI18N
        lbUser2.setText("User");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(showRequest)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAccept))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lbUser2, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showRequest)
                    .addComponent(btnAccept))
                .addGap(19, 19, 19)
                .addComponent(lbUser2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );

        jTabbedPane1.addTab("Lời mời kết bạn", jPanel6);

        tblListFriend.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        tblListFriend.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Username", "Email"
            }
        ));
        jScrollPane4.setViewportView(tblListFriend);

        btnResetFriendList.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        btnResetFriendList.setText("Reset");
        btnResetFriendList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetFriendListActionPerformed(evt);
            }
        });

        lbUser3.setFont(new java.awt.Font("Tekton Pro", 0, 24)); // NOI18N
        lbUser3.setText("User");

        jButton2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton2.setText("Xem trang cá nhân");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lbUser3, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnResetFriendList, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnResetFriendList)
                    .addComponent(jButton2))
                .addGap(11, 11, 11)
                .addComponent(lbUser3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );

        jTabbedPane1.addTab("Danh Sách Bạn Bè", jPanel7);

        ListOnlineFriend.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        ListOnlineFriend.setFont(new java.awt.Font("Tekton Pro", 0, 16)); // NOI18N
        ListOnlineFriend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ListOnlineFriendMouseClicked(evt);
            }
        });
        userListPane.setViewportView(ListOnlineFriend);

        lbMsg.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        lbMsg.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbMsg.setText("Messenger");

        lbUser4.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        lbUser4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbUser4.setText("User");

        jLabel4.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        jLabel4.setText("Online Friend List");
        jLabel4.setFocusable(false);

        ChatTextPanel.setEditable(false);
        ChatTextPanel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane6.setViewportView(ChatTextPanel);

        txtMsg.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMsgActionPerformed(evt);
            }
        });
        txtMsg.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMsgKeyPressed(evt);
            }
        });

        btnSendMsg.setFont(new java.awt.Font("Tekton Pro", 0, 18)); // NOI18N
        btnSendMsg.setText("Send");
        btnSendMsg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMsgActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbUser4, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(623, 623, 623)
                        .addComponent(jLabel4)))
                .addContainerGap(35, Short.MAX_VALUE))
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addComponent(txtMsg))
                .addGap(18, 18, 18)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userListPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                        .addComponent(btnSendMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbUser4)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6)
                    .addComponent(userListPane, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSendMsg)
                    .addComponent(txtMsg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );

        jTabbedPane1.addTab("Nhắn tin", jPanel9);

        jLabel5.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Group List");
        jLabel5.setFocusable(false);

        ListGroup.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        ListGroup.setFont(new java.awt.Font("Tekton Pro", 0, 16)); // NOI18N
        ListGroup.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ListGroupMouseClicked(evt);
            }
        });
        userListPane1.setViewportView(ListGroup);

        ChatTextGroupPanel.setEditable(false);
        ChatTextGroupPanel.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jScrollPane8.setViewportView(ChatTextGroupPanel);

        lbUser5.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        lbUser5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbUser5.setText("User");

        lbMsg1.setFont(new java.awt.Font("Tekton Pro", 1, 18)); // NOI18N
        lbMsg1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbMsg1.setText("Msg #Group");

        txtMsgGroup.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtMsgGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMsgGroupActionPerformed(evt);
            }
        });
        txtMsgGroup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMsgGroupKeyPressed(evt);
            }
        });

        btnSendMsgGroup.setFont(new java.awt.Font("Tekton Pro", 0, 18)); // NOI18N
        btnSendMsgGroup.setText("Send");
        btnSendMsgGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMsgGroupActionPerformed(evt);
            }
        });

        btnCreatGroup.setFont(new java.awt.Font("Tekton Pro", 0, 18)); // NOI18N
        btnCreatGroup.setText("Creat A Group");
        btnCreatGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreatGroupActionPerformed(evt);
            }
        });

        btnJoinGroup.setFont(new java.awt.Font("Tekton Pro", 0, 18)); // NOI18N
        btnJoinGroup.setText("Join A Group");
        btnJoinGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnJoinGroupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lbMsg1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbUser5, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnCreatGroup)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnJoinGroup, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                    .addComponent(jScrollPane8)
                    .addComponent(txtMsgGroup))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(userListPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(btnSendMsgGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(35, 35, 35))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCreatGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lbMsg1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lbUser5)
                        .addComponent(jLabel5)
                        .addComponent(btnJoinGroup)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8)
                    .addComponent(userListPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSendMsgGroup)
                    .addComponent(txtMsgGroup, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );

        jTabbedPane1.addTab("Group Chat", jPanel2);

        txtThongBao.setEditable(false);
        txtThongBao.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        txtThongBao.setColumns(20);
        txtThongBao.setFont(new java.awt.Font("Tekton Pro", 0, 18)); // NOI18N
        txtThongBao.setRows(5);
        jScrollPane7.setViewportView(txtThongBao);

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 767, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Thông báo", jPanel10);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Username: ");

        txtUsername.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel7.setText("email");

        txtEmail.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setText("Avatar");

        lbImage.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lbImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbImage.setText("Ảnh đại diện");

        jButton1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jButton1.setText("Update");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        txtAvt.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        txtAvt.setText("Change");
        txtAvt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAvtActionPerformed(evt);
            }
        });

        txtCfPass.setText("jPasswordField1");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel10.setText("password");

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel11.setText("Confirm Password");

        txtPass.setText("jPasswordField1");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtAvt, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(106, 106, 106))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel8)
                                .addGap(43, 43, 43)))
                        .addComponent(lbImage, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(61, 61, 61))))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCfPass, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtPass, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))
                .addContainerGap(457, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtCfPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbImage, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(txtAvt))
                .addContainerGap(212, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Quản Lý Tài Khoản", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSendMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMsgActionPerformed
        // TODO add your handling code here:
        int row = ListOnlineFriend.getSelectedIndex();
        if (row < 0 || row > ListOnlineFriend.getMaxSelectionIndex()) {
            showMessage("Please choose  a friend!");
        } else {
            String to_username = (String) ListOnlineFriend.getSelectedValue();
            if (to_username != null && !to_username.equals("") && !to_username.equals(" ")) {
                int from_id = client.getUser().getId();
                String msgText = txtMsg.getText().trim();
                if (msgText.equals("") || msgText == null) {
                    showMessage("Please enter some word!");
                } else {
                    String msg = from_id + " " + to_username + " " + msgText;
                    client.sendData(new ObjectWrapper(ObjectWrapper.SEND_MESSAGE, msg));
                    txtMsg.setText("");
                }
            } else {
                showMessage("Please choose  a friend!");

            }
        }

    }//GEN-LAST:event_btnSendMsgActionPerformed

    private void txtMsgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMsgActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMsgActionPerformed

    private void ListOnlineFriendMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ListOnlineFriendMouseClicked
        // TODO add your handling code here:
        String to_username = (String) ListOnlineFriend.getSelectedValue();
        if (to_username != null && !to_username.equals("") && !to_username.equals(" ")) {
            int from_id = client.getUser().getId();
            client.sendData(new ObjectWrapper(ObjectWrapper.SHOW_LIST_MESSAGE, from_id + " " + to_username));
        }

    }//GEN-LAST:event_ListOnlineFriendMouseClicked

    private void btnResetFriendListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetFriendListActionPerformed
        // TODO add your handling code here:
        showListFriend();
    }//GEN-LAST:event_btnResetFriendListActionPerformed

    private void btnAcceptActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAcceptActionPerformed
        int row = tblFriendRequest.getSelectedRow();
        if (row < 0 || row > tblFriendRequest.getRowCount()) {
            showMessage("Please choose a user!");
        } else {
            int to_id = (int) tableFrRequest.getValueAt(row, 0);
            String from_id = client.getUser().getId() + "";
            ObjectWrapper data = new ObjectWrapper(ObjectWrapper.ADD_FRIEND, from_id + " " + to_id);
            client.sendData(data);
        }
    }//GEN-LAST:event_btnAcceptActionPerformed

    private void showRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showRequestActionPerformed
        // TODO add your handling code here:
        showFriendRequest();
    }//GEN-LAST:event_showRequestActionPerformed

    private void txtUpdateStatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtUpdateStatusKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String stt = txtUpdateStatus.getText().trim();
            if (stt.equals("") || stt == null) {
                showMessage("Please enter some word!");
            } else {
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String time = sdf.format(d);
                String msg = "User " + client.getUser().getUsername() + " has updated his/hser status at " + time + ":";
                msg = msg + "\n" + stt;
                client.sendData(new ObjectWrapper(ObjectWrapper.UPDATE_STATUS, msg));
                txtUpdateStatus.setText("  What's in your mine?");
            }
        }
    }//GEN-LAST:event_txtUpdateStatusKeyPressed

    private void btnViewProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewProfileActionPerformed
        // TODO add your handling code here:
        int row = tblFriend.getSelectedRow();
        if (row < 0 || row > tblFriend.getRowCount()) {
            showMessage("Please choose a user!");
        } else {
            int user_id = (int) tableFriend.getValueAt(row, 0);
            try {
                User user = i.getUserByID(user_id);
                 byte[] b = i.retriveImg(user);
                 new ViewProfileDig(this, rootPaneCheckingEnabled, user, b).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không thể thực hiệm hàm RMI");
            }

        }
    }//GEN-LAST:event_btnViewProfileActionPerformed

    private void btnSearchPlayerByNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchPlayerByNameActionPerformed
        // TODO add your handling code here:
        String key = txtKey.getText();
        String username = client.getUser().getUsername();
        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SEARCH_USER_BY_NAME, username + " " + key);
        client.sendData(data);
    }//GEN-LAST:event_btnSearchPlayerByNameActionPerformed

    private void txtMsgKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMsgKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            int row = ListOnlineFriend.getSelectedIndex();
            if (row < 0 || row > ListOnlineFriend.getMaxSelectionIndex()) {
                showMessage("Please choose  a friend!");
            } else {
                String to_username = (String) ListOnlineFriend.getSelectedValue();
                int from_id = client.getUser().getId();
                String msgText = txtMsg.getText().trim();
                if (msgText.equals("") || msgText == null) {
                    showMessage("Please enter some word!");
                } else {
                    String msg = from_id + " " + to_username + " " + msgText;
                    client.sendData(new ObjectWrapper(ObjectWrapper.SEND_MESSAGE, msg));
                    txtMsg.setText("");
                }

            }
        }

    }//GEN-LAST:event_txtMsgKeyPressed

    private void btnSendMsgGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMsgGroupActionPerformed
        // TODO add your handling code here:
        int row = ListGroup.getSelectedIndex();
        if (row < 0 || row > ListGroup.getMaxSelectionIndex()) {
            showMessage("Please choose  a Group!");
        } else {
            String to_group = (String) ListGroup.getSelectedValue();
            if (to_group != null && !to_group.equals("") && !to_group.equals(" ")) {
                int user_id = client.getUser().getId();
                String msgText = txtMsgGroup.getText().trim();
                if (msgText.equals("") || msgText == null) {
                    showMessage("Please enter some word!");
                } else {
                    msgText = client.getUser().getUsername() + ": " + msgText;
                    String msg = user_id + " " + to_group + " " + msgText;
                    client.sendData(new ObjectWrapper(ObjectWrapper.SEND_GROUP_MESSAGE, msg));
                    txtMsgGroup.setText("");
                }
            } else {
                showMessage("Please choose  a Group!");

            }
        }
    }//GEN-LAST:event_btnSendMsgGroupActionPerformed

    private void txtMsgGroupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMsgGroupKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            int row = ListGroup.getSelectedIndex();
            if (row < 0 || row > ListGroup.getMaxSelectionIndex()) {
                showMessage("Please choose  a Group!");
            } else {
                String to_group = (String) ListGroup.getSelectedValue();
                if (to_group != null && !to_group.equals("") && !to_group.equals(" ")) {
                    int user_id = client.getUser().getId();
                    String msgText = txtMsgGroup.getText().trim();
                    if (msgText.equals("") || msgText == null) {
                        showMessage("Please enter some word!");
                    } else {
                        msgText = client.getUser().getUsername() + ": " + msgText;
                        String msg = user_id + " " + to_group + " " + msgText;
                        client.sendData(new ObjectWrapper(ObjectWrapper.SEND_GROUP_MESSAGE, msg));
                        txtMsgGroup.setText("");
                    }
                } else {
                    showMessage("Please choose  a Group!");

                }
            }

        }

    }//GEN-LAST:event_txtMsgGroupKeyPressed

    private void txtMsgGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMsgGroupActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMsgGroupActionPerformed

    private void ListGroupMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ListGroupMouseClicked
        // TODO add your handling code here:
        String gr_name = (String) ListGroup.getSelectedValue();
        if (gr_name != null && !gr_name.equals("") && !gr_name.equals(" ")) {
            int user_id = client.getUser().getId();
            client.sendData(new ObjectWrapper(ObjectWrapper.SHOW_LIST_GROUP_MESSAGE, user_id + " " + gr_name));
        }
    }//GEN-LAST:event_ListGroupMouseClicked

    private void btnCreatGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreatGroupActionPerformed
        // TODO add your handling code here:
        new CreateGroupDig(this, rootPaneCheckingEnabled, client).setVisible(true);
    }//GEN-LAST:event_btnCreatGroupActionPerformed

    private void btnJoinGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnJoinGroupActionPerformed
        // TODO add your handling code here:
        new JoinGroupDig(this, rootPaneCheckingEnabled, client).setVisible(true);

    }//GEN-LAST:event_btnJoinGroupActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        if (!txtUsername.getText().trim().equals("") && !txtPass.getText().trim().equals("")) {
            try {
                if (!txtUsername.getText().equals(this.client.getUser().getUsername())) {
                    if (i.isExist(txtUsername.getText()) == 1) {
                        JOptionPane.showMessageDialog(null, "Tên tài khoản đã tồn tại");
                    } else {
                        if (txtCfPass.getText().trim().equals(txtPass.getText().trim())) {
                            User u = new User();
                            u.setId(this.client.getUser().getId());
                            u.setUsername(txtUsername.getText().trim());
                            u.setEmail(txtEmail.getText().trim());
                            u.setPassword(txtPass.getText().trim());
                            i.update(u);
                            JOptionPane.showMessageDialog(null, "Cập nhật thành công");
                            new LoginFrm().setVisible(true);
                            handleClose();
                            this.dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, "Mật khẩu không trùng");
                        }

                    }
                } else {
                    if (txtCfPass.getText().trim().equals(txtPass.getText().trim())) {
                        User u = new User();
                        u.setId(this.client.getUser().getId());
                        u.setUsername(txtUsername.getText().trim());
                        u.setEmail(txtEmail.getText().trim());
                        u.setPassword(txtPass.getText().trim());
                        i.update(u);
                        JOptionPane.showMessageDialog(null, "Cập nhật thành công");
                        new LoginFrm().setVisible(true);
                        handleClose();
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Mật khẩu không trùng");
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không thể thực hiệm hàm RMI");
            }

        } else {
            JOptionPane.showMessageDialog(null, "Vui lòng điền đầy đủ thông tin!");
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void txtAvtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAvtActionPerformed
        // TODO add your handling code here:
        try {
            JFileChooser jf = new JFileChooser();
            // Show open dialog
            int option = jf.showOpenDialog(this);
            // If user chooses to insert..
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = jf.getSelectedFile();
                if (i.updateImg(this.client.getUser(), file) == 1) {
                    JOptionPane.showMessageDialog(null, "Cập nhật ảnh đai diện thành công");
                    setImageUser();
                } else {
                    JOptionPane.showMessageDialog(null, "Lỗi thực hiện hàm RMI");
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể thực hiệm hàm RMI");
        }

    }//GEN-LAST:event_txtAvtActionPerformed

    private void btnAddFriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFriendActionPerformed
        // TODO add your handling code here:
        int row = tblFriend.getSelectedRow();
        if (row < 0 || row > tblFriend.getRowCount()) {
            showMessage("Please choose a user!");
        } else {
            int to_id = (int) tableFriend.getValueAt(row, 0);
            String from_id = client.getUser().getId() + "";
            ObjectWrapper data = new ObjectWrapper(ObjectWrapper.ADD_FRIEND_REQUEST, from_id + " " + to_id);
            client.sendData(data);
        }
    }//GEN-LAST:event_btnAddFriendActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        int row = tblListFriend.getSelectedRow();
        if (row < 0 || row > tblListFriend.getRowCount()) {
            showMessage("Please choose a user!");
        } else {
            int user_id = (int) tableListFriend.getValueAt(row, 0);
            try {
                User user = i.getUserByID(user_id);
                 byte[] b = i.retriveImg(user);
                 new ViewProfileDig(this, rootPaneCheckingEnabled, user, b).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Không thể thực hiệm hàm RMI");
            }

        }
    }//GEN-LAST:event_jButton2ActionPerformed
    private void showFriendRequest() {
        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SHOW_FRIEND_REQUEST, client.getUser().getId() + "");
        client.sendData(data);

    }

    public void showListFriend() {
        ObjectWrapper data = new ObjectWrapper(ObjectWrapper.SHOW_LIST_FRIEND, client.getUser().getId() + "");
        client.sendData(data);

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane ChatTextGroupPanel;
    private javax.swing.JTextPane ChatTextPanel;
    private javax.swing.JList ListGroup;
    private javax.swing.JList ListOnlineFriend;
    private javax.swing.JTextPane NewsPanel;
    private javax.swing.JButton btnAccept;
    private javax.swing.JButton btnAddFriend;
    private javax.swing.JButton btnCreatGroup;
    private javax.swing.JButton btnJoinGroup;
    private javax.swing.JButton btnResetFriendList;
    private javax.swing.JButton btnSearchPlayerByName;
    private javax.swing.JButton btnSendMsg;
    private javax.swing.JButton btnSendMsgGroup;
    private javax.swing.JButton btnViewProfile;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbImage;
    private javax.swing.JLabel lbMsg;
    private javax.swing.JLabel lbMsg1;
    private javax.swing.JLabel lbUser1;
    private javax.swing.JLabel lbUser2;
    private javax.swing.JLabel lbUser3;
    private javax.swing.JLabel lbUser4;
    private javax.swing.JLabel lbUser5;
    private javax.swing.JLabel lbUserMain;
    private javax.swing.JButton showRequest;
    private javax.swing.JTable tblFriend;
    private javax.swing.JTable tblFriendRequest;
    private javax.swing.JTable tblListFriend;
    private javax.swing.JButton txtAvt;
    private javax.swing.JPasswordField txtCfPass;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtKey;
    private javax.swing.JTextField txtMsg;
    private javax.swing.JTextField txtMsgGroup;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JTextArea txtThongBao;
    private javax.swing.JTextField txtUpdateStatus;
    private javax.swing.JTextField txtUsername;
    private javax.swing.JScrollPane userListPane;
    private javax.swing.JScrollPane userListPane1;
    // End of variables declaration//GEN-END:variables
}
