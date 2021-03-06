import com.google.common.base.CaseFormat;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

public class FlutterTemplateAction extends AnAction {
    private Project project;
    private String psiPath;
    /**
     * Overall popup entity
     */
    private JDialog jDialog;
    private JTextField nameTextField;
    private ButtonGroup templateGroup;
    /**
     * Checkbox
     * Use folder：default true
     * Use prefix：default false
     */
    private JCheckBox folderBox, prefixBox;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getProject();
        psiPath = event.getData(PlatformDataKeys.PSI_ELEMENT).toString();
        psiPath = psiPath.substring(psiPath.indexOf(":") + 1);
        initView();
    }

    private void initView() {
        jDialog = new JDialog(new JFrame(), "Flutter Template Code Produce");
        //Set function button
        Container container = jDialog.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

        //Set the main module style: mode, function
        setModule(container);

        //Setting options: whether to use prefix
        setCodeFile(container);

        //Generate file name and OK cancel button
        setNameAndConfirm(container);

        //Choose a pop-up style
        setJDialog();
    }

    /**
     * Set the overall pop-up style
     */
    private void setJDialog() {
        //The focus is on the current pop-up window,
        // and the focus will not shift even if you click on other areas
        jDialog.setModal(true);
        //Set padding
        ((JPanel) jDialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jDialog.setSize(400, 320);
        jDialog.setLocationRelativeTo(null);
        jDialog.setVisible(true);
    }

    /**
     * Main module
     *
     * @param container
     */
    private void setModule(Container container) {
        //Two rows and two columns
        JPanel template = new JPanel();
        template.setLayout(new GridLayout(1, 2));
        //Set the main module style：mode, function
        template.setBorder(BorderFactory.createTitledBorder("Select Mode"));

        JRadioButton pageBtn = new JRadioButton("Page", true);
        pageBtn.setActionCommand("Page");
        setPadding(pageBtn, 5, 10);

        JRadioButton viewBtn = new JRadioButton("View", true);
        viewBtn.setActionCommand("View");
        setPadding(viewBtn, 5, 10);

        template.add(pageBtn);
        templateGroup = new ButtonGroup();
        templateGroup.add(pageBtn);

        template.add(viewBtn);
        templateGroup.add(viewBtn);

        container.add(template);
        setDivision(container);
    }


    /**
     * Generate file
     *
     * @param container
     */
    private void setCodeFile(Container container) {
        //Select build file
        JPanel file = new JPanel();
        file.setLayout(new GridLayout(1, 2));
        file.setBorder(BorderFactory.createTitledBorder("Select Function"));

        //Use folder
        folderBox = new JCheckBox("Folder", true);
        setMargin(folderBox, 5, 10);
        file.add(folderBox);

        //Use prefix
        prefixBox = new JCheckBox("Prefix", false);
        setMargin(prefixBox, 5, 10);
        file.add(prefixBox);

        container.add(file);
        setDivision(container);
    }


    /**
     * Generate file name and button
     *
     * @param container
     */
    private void setNameAndConfirm(Container container) {
        JPanel nameField = new JPanel();
        nameField.setLayout(new FlowLayout());
        nameField.setBorder(BorderFactory.createTitledBorder("Module Name"));
        nameTextField = new JTextField(30);
        nameTextField.addKeyListener(keyListener);
        nameField.add(nameTextField);
        container.add(nameField);

        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout());

        //Set bottom spacing
        setDivision(container);

        //OK cancel button
        JButton cancel = new JButton("Cancel");
        cancel.setForeground(JBColor.RED);
        cancel.addActionListener(actionListener);

        JButton ok = new JButton("OK");
        ok.setForeground(JBColor.GREEN);
        ok.addActionListener(actionListener);
        menu.add(cancel);
        menu.add(ok);
        container.add(menu);
    }


    private void save() {
        if (nameTextField.getText() == null || "".equals(nameTextField.getText().trim())) {
            Messages.showInfoMessage(project, "Please input the module name", "Info");
            return;
        }
        dispose();
        //Create a file
        createFile();
        //Refresh project
        project.getBaseDir().refresh(false, true);
    }

    private void createFile() {
        String type = templateGroup.getSelection().getActionCommand();
        String name = nameTextField.getText();
        String prefix = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        String folder = "";
        String prefixName = "";

        //Add folder
        if (folderBox.isSelected()) {
            folder = "/" + prefix;
        }

        //Add prefix
        if (prefixBox.isSelected()) {
            prefixName = prefix + "_";
        }

        switch (type) {
            case "Page":
                generatePage(folder, prefixName);
                break;
            case "View":
                generateView(folder, prefixName);
                break;
        }
    }

    private void generatePage(String folder, String prefixName) {
        String path = psiPath + folder;
        generateFile("state.dart", path, prefixName + "state.dart");
        generateFile("vm.dart", path, prefixName + "vm.dart");
        generateFile("page.dart", path, prefixName + "page.dart");
    }

    private void generateView(String folder, String prefixName) {
        String path = psiPath + folder;
        generateFile("state.dart", path, prefixName + "state.dart");
        generateFile("vm.dart", path, prefixName + "vm.dart");
        generateFile("view.dart", path, prefixName + "view.dart");
    }


    private void generateFile(String inputFileName, String filePath, String outFileName) {
        //Get file content
        String content = "";
        try {
            InputStream in = this.getClass().getResourceAsStream("/templates/" + inputFileName);
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        content = content.replaceAll("\\$name", nameTextField.getText());
        //Adding a prefix requires modifying the imported class name
        if (prefixBox.isSelected()) {
            String prefixName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, nameTextField.getText()) + "_";
            content = content.replaceAll("\'vm.dart\'", "\'" + prefixName + "vm.dart" + "\'");
            content = content.replaceAll("\'state.dart\'", "\'" + prefixName + "state.dart" + "\'");
        }

        //Write file
        try {
            File folder = new File(filePath);
            // if file doesnt exists, then create it
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(filePath + "/" + outFileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }


    private KeyListener keyListener = new KeyListener() {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) save();
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    };

    private ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Cancel")) {
                dispose();
            } else {
                save();
            }
        }
    };

    private void setPadding(JRadioButton btn, int top, int bottom) {
        btn.setBorder(BorderFactory.createEmptyBorder(top, 10, bottom, 0));
    }

    private void setMargin(JCheckBox btn, int top, int bottom) {
        btn.setBorder(BorderFactory.createEmptyBorder(top, 10, bottom, 0));
    }

    private void setDivision(Container container) {
        //Separate the spacing between modules
        JPanel margin = new JPanel();
        container.add(margin);
    }

    private void dispose() {
        jDialog.dispose();
    }
}
