package com.dekagames.atlasmaker;

import com.dekagames.atlasmaker.algorithm.PackAlgorithm;
import com.dekagames.slon.Slon;
import com.dekagames.slon.SlonException;
import com.dekagames.slon.SlonNode;
import net.miginfocom.swing.MigLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: Deka
 * Date: 30.06.13
 * Time: 20:53
 * To change this template use File | Settings | File Templates.
 */
public class MainWindow extends JFrame implements ActionListener {
    public static File      CURRENT_DIR;
    public static File      CURRENT_PRJ_FILE;
    public static boolean   NEED_TO_SAVE;

    private     JToolBar    toolbarMain, toolbarSprites, toolbarFonts, toolbarRight; // основной тулбар, и левый тулбар (для спрайтов)

    private     JButton     btnNew, btnOpen, btnSave, btnPack;              // кнопки основного тулбара
    private     JButton     btnAddSprite, btnDelSprite, btnEditSprite;      // кнопки тулбара спрайтовой панели
    private     JButton     btnAddFont, btnDelFont, btnEditFont;            // кнопки тулбара спрайтовой панели

    private     JButton     btnZoomIn, btnZoomOut;                  // кнопки тулбара итогового атласа
    // пункты меню
    private     JMenuItem   mnuFileNew, mnuFileOpen, mnuFileSave, mnuFileSaveAs, mnuFileExit;
    private     JMenuItem   mnuAtlasPack;
    // левая панель - список спрайтов
    private     JPanel      panelLeft;
    private     JList       listSprites;
    private     JList       listFonts;
    private     DefaultListModel spriteListModel, fontListModel;
    private     JTabbedPane tabsPane;
    private     JPanel      spriteTabPanel, fontTabPanel;
    // нижняя панель - настройки экспорта атласа
    private     String[]    ATLAS_DIMENSION = {"128","256","512","1024","2048","4096"};     // POT sizes for atlas
    private     JPanel      panelBottom;
    private     JComboBox   comboWidth, comboHeight;                // комбобоксы для выбора ширины и высоты атласа
    private     JTextField  textExportPath;                         // поле для ввода пути файла
    private     JButton     btnBrowse;

    // правая панель - итоговый атлас
    private     JPanel      panelRight;
    private     AtlasPanel  atlasPanel;
    private     JScrollPane atlasScroll;        // скролл панель для картинки

    // прочие элементы
    private     JFileChooser    projectFileChooser;

    public  SpriteTree  spriteTree;
    public  FontTree    fontTree;

    public MainWindow(){
        super("Atlas Maker");
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        NEED_TO_SAVE = false;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // сделаем свой выход с запросом сохранения если не сохранено
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cmdFileExit();
            }
        });

        initControls();
        setSize(1000, 700);
        spriteTree = new SpriteTree();
        fontTree = new FontTree();
        setTitle("[Untitled] - Atlas Maker");
    }

    private void initControls() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenu menuAtlas = new JMenu("Atlas");
        JMenu menuHelp = new JMenu("Help");

        // -------------------------- главное меню ----------------------------------------
        mnuFileNew = new JMenuItem("New project");      mnuFileNew.addActionListener(this);
        mnuFileOpen = new JMenuItem("Open project");    mnuFileOpen.addActionListener(this);
        mnuFileSave = new JMenuItem("Save project");    mnuFileSave.addActionListener(this);
        mnuFileSaveAs = new JMenuItem("Save as...");    mnuFileSaveAs.addActionListener(this);
        mnuFileExit = new JMenuItem("Exit");            mnuFileExit.addActionListener(this);

        mnuAtlasPack = new JMenuItem("Pack");           mnuAtlasPack.addActionListener(this);

        menuFile.add(mnuFileNew);
        menuFile.add(mnuFileOpen);
        menuFile.add(mnuFileSave);
        menuFile.add(mnuFileSaveAs);
        menuFile.add(new JSeparator());
        menuFile.add(mnuFileExit);

        menuAtlas.add(mnuAtlasPack);

        menuBar.add(menuFile);
        menuBar.add(menuAtlas);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);

        // -------------------------- главный тулбар ----------------------------------------

        btnNew = new JButton(new ImageIcon(getClass().getResource("media/NewFile.png")));
        btnNew.setToolTipText("New sprite atlas project");
        btnNew.addActionListener(this);

        btnOpen = new JButton(new ImageIcon(getClass().getResource("media/OpenFile.png")));
        btnOpen.setToolTipText("Open sprite atlas project");
        btnOpen.addActionListener(this);

        btnSave = new JButton(new ImageIcon(getClass().getResource("media/SaveFile.png")));
        btnSave.setToolTipText("Save current project");
        btnSave.addActionListener(this);

        btnPack = new JButton(new ImageIcon(getClass().getResource("media/PackAtlas.png")));
        btnPack.setToolTipText("Pack atlas of the current project");
        btnPack.addActionListener(this);

        toolbarMain = new JToolBar();
        toolbarMain.setFloatable(false);
        toolbarMain.add(btnNew);
        toolbarMain.add(btnOpen);
        toolbarMain.add(btnSave);
        toolbarMain.add(new JToolBar.Separator());
        toolbarMain.add(btnPack);
        add(toolbarMain, BorderLayout.NORTH);

        // -------------------------- левая панель со своим тулбаром и т.д. ------------------------
        // кнопки левого тулбара
        btnAddSprite = new JButton(new ImageIcon(getClass().getResource("media/AddSprite.png")));
        btnAddSprite.setToolTipText("Add sprite to project");
        btnAddSprite.addActionListener(this);

        btnDelSprite = new JButton(new ImageIcon(getClass().getResource("media/DelSprite.png")));
        btnDelSprite.setToolTipText("Remove sprite from project");
        btnDelSprite.addActionListener(this);

        btnEditSprite = new JButton(new ImageIcon(getClass().getResource("media/EditSprite.png")));
        btnEditSprite.setToolTipText("Edit sprite (double click)");
        btnEditSprite.addActionListener(this);

        // сам левый тулбар
        toolbarSprites = new JToolBar();
        toolbarSprites.setFloatable(false);
        toolbarSprites.add(btnAddSprite);
        toolbarSprites.add(btnDelSprite);
        toolbarSprites.add(new JToolBar.Separator());
        toolbarSprites.add(btnEditSprite);

        // -------------------------- панель шрифтов со своим тулбаром и т.д. ------------------------
        // кнопки левого тулбара
        btnAddFont = new JButton(new ImageIcon(getClass().getResource("media/AddFont.png")));
        btnAddFont.setToolTipText("Add font to project");
        btnAddFont.addActionListener(this);

        btnDelFont = new JButton(new ImageIcon(getClass().getResource("media/DelSprite.png")));
        btnDelFont.setToolTipText("Remove font from project");
        btnDelFont.addActionListener(this);

        btnEditFont = new JButton(new ImageIcon(getClass().getResource("media/EditSprite.png")));
        btnEditFont.setToolTipText("Edit font (double click)");
        btnEditFont.addActionListener(this);

        // сам левый тулбар
        toolbarFonts = new JToolBar();
        toolbarFonts.setFloatable(false);
        toolbarFonts.add(btnAddFont);
        toolbarFonts.add(btnDelFont);
        toolbarFonts.add(new JToolBar.Separator());
        toolbarFonts.add(btnEditFont);

        // панель с вкладками
        tabsPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        // вкладка со спрайтами
        spriteTabPanel = new JPanel(new BorderLayout());
        // вкладка со шрифтами
        fontTabPanel = new JPanel(new BorderLayout());

        // нижняя панель с настройками экспорта
        panelBottom = new JPanel(new MigLayout("","[] [] [] [] [grow]"));
        comboWidth = new JComboBox(ATLAS_DIMENSION);
        comboHeight = new JComboBox(ATLAS_DIMENSION);
        textExportPath = new JTextField(5000);
        btnBrowse = new JButton("Browse...");
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser exportFileChooser = new JFileChooser(new File(textExportPath.getText()));
                int returnVal = exportFileChooser.showSaveDialog(MainApplication.mainWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = exportFileChooser.getSelectedFile();
                    String  filePath = file.getAbsolutePath();
                    String  fileExt = getFileExtension(filePath);
                    if (fileExt== null || !fileExt.equalsIgnoreCase("png")) filePath += ".png";
                    // занесем имя в edit
                    textExportPath.setText(filePath);
                }
            }
        });

        panelBottom.add(new JLabel("Atlas size:"));
        panelBottom.add(comboWidth);
        panelBottom.add(new JLabel(" X "));
        panelBottom.add(comboHeight,"wrap");
        panelBottom.add(new JLabel("Export atlas to file:"));
        panelBottom.add(textExportPath,"span 3");
        panelBottom.add(btnBrowse);

        // список спрайтов
        spriteListModel = new DefaultListModel();
        listSprites = new JList(spriteListModel);
        listSprites.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                cmdListSpritesClicked(e);
            }
        });

        // список шрифтов
        fontListModel = new DefaultListModel();
        listFonts = new JList(fontListModel);
        listFonts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                cmdListFontsClicked(e);
            }
        });

        // заполним вкладку со спрайтами
        spriteTabPanel.add(new JScrollPane(listSprites),BorderLayout.CENTER);
        spriteTabPanel.add(toolbarSprites,BorderLayout.NORTH);
        tabsPane.addTab("Sprites", spriteTabPanel);

        // заполним вкладку со шрифтами
        fontTabPanel.add(new JScrollPane(listFonts),BorderLayout.CENTER);
        fontTabPanel.add(toolbarFonts, BorderLayout.NORTH);
        tabsPane.addTab("Fonts",fontTabPanel);

        // левая панель
        panelLeft = new JPanel(new BorderLayout());
        panelLeft.add(tabsPane, BorderLayout.CENTER);
//        panelLeft.add(new JScrollPane(listSprites),BorderLayout.CENTER);
//        panelLeft.add(toolbarSprites,BorderLayout.NORTH);
        panelLeft.add(panelBottom, BorderLayout.SOUTH);

        // правый тулбар
        btnZoomIn = new JButton(new ImageIcon(getClass().getResource("media/zoomin.png")));
        btnZoomIn.setToolTipText("Zoom in");
        btnZoomIn.addActionListener(this);

        btnZoomOut = new JButton(new ImageIcon(getClass().getResource("media/zoomout.png")));
        btnZoomOut.setToolTipText("Zoom out");
        btnZoomOut.addActionListener(this);

        toolbarRight = new JToolBar();
        toolbarRight.setFloatable(false);
        toolbarRight.add(btnZoomIn);
        toolbarRight.add(btnZoomOut);

        // рисовательная панель - на ней рисуется атлас
        atlasPanel = new AtlasPanel();
        atlasScroll = new JScrollPane(atlasPanel);
        atlasScroll.setAutoscrolls(true);

        // правая панель
        panelRight = new JPanel(new BorderLayout());
        panelRight.add(toolbarRight,BorderLayout.NORTH);
        panelRight.add(atlasScroll,BorderLayout.CENTER);


        JSplitPane splitMain = new JSplitPane();
        splitMain.setLeftComponent(panelLeft);
        splitMain.setRightComponent(panelRight);
        add(splitMain);

        // прочие элементы
        projectFileChooser = new JFileChooser();
        projectFileChooser.setFileFilter(new FileNameExtensionFilter("Packer project", "packer"));

    }



    /**
     * приводит в соответствие список спрайтов и шрифтов на экране к списку папок в памяти
     */
    public final void updateList(){
        // очистим список
        spriteListModel.clear();
        fontListModel.clear();
        // добавим все папки в список
        for (int i=0; i< spriteTree.getCount(); i++) {
            SpriteNode sprite = spriteTree.getNodeAt(i);
            spriteListModel.addElement(sprite.toString());
        }
        for (int i=0; i< fontTree.getCount(); i++) {
            FontNode font = fontTree.getNodeAt(i);
            fontListModel.addElement(font.toString());
        }
    }

    // menu handlers
    private void cmdFileNew(ActionEvent e){
        if (NEED_TO_SAVE){
            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Atlas MMaker",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
            if (answer == JOptionPane.YES_OPTION)
                cmdFileSave();
            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
                return;
        }

        spriteTree.clear();
        fontTree.clear();
        atlasPanel.atlasImage = null;
        updateList();

        CURRENT_PRJ_FILE=null;
        NEED_TO_SAVE = true;
        setTitle("[Untitled] - Atlas Maker");
    }

    private void cmdFileOpen(ActionEvent e){
        if (NEED_TO_SAVE){
            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Atlas MMaker",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
            if (answer == JOptionPane.YES_OPTION)
                cmdFileSave();
            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
                return;
        }

        projectFileChooser.setCurrentDirectory(CURRENT_DIR);
        int retVal = projectFileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            CURRENT_PRJ_FILE = projectFileChooser.getSelectedFile();
            String  filePath = CURRENT_PRJ_FILE.getAbsolutePath();
            setTitle("["+filePath+"] - Atlas Maker");

            // откроем слон файл
            Slon slon = new Slon();
            try {
                slon.load(filePath);
            } catch (SlonException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                CURRENT_PRJ_FILE = null;
                setTitle("[Untitled] - Atlas Maker");
            }

            SlonNode rootNode = slon.getRoot();
            // ширина и высота атласа
            comboWidth.setSelectedItem(rootNode.getKeyValue("width"));
            comboHeight.setSelectedItem(rootNode.getKeyValue("height"));
            // путь экспорта
            textExportPath.setText(rootNode.getKeyValue("export"));

            // создадим дерево спрайтов в памяти
            spriteTree = new SpriteTree();
            SlonNode spritesNode = rootNode.getChildWithKeyValue("name", "sprites");
            if (spritesNode == null)
                JOptionPane.showMessageDialog(this, "Could not find sprites node!");
            else {
                for (int i=0; i<spritesNode.getChildCount(); i++){
                    SlonNode slonNode = spritesNode.getChildAt(i);
                    SpriteNode spriteNode = new SpriteNode(slonNode.getKeyValue("name"));
                    // название спрайта и его характеристики
                    spriteNode.isOutlineWithBorder = slonNode.getKeyAsBoolean("outline");
                    spriteNode.isCropped = slonNode.getKeyAsBoolean("crop");
                    spriteNode.transparentBorder = slonNode.getKeyAsInt("border");

                    for (int j=0; j<slonNode.getChildCount(); j++){
                        SlonNode picN = slonNode.getChildAt(j);
                        // запишем имена файлов отдельного кадра и их характеристики
                        FrameNode pctr = new FrameNode(picN.getKeyValue("file"));
                        pctr.xPivot = picN.getKeyAsInt("pivotX");
                        pctr.yPivot = picN.getKeyAsInt("pivotY");
                        spriteNode.addElement(pctr);
                    }
                    spriteTree.addSprite(spriteNode);
                }

                // создадим дерево шрифтов в памяти
                fontTree = new FontTree();
                SlonNode fontsNode = rootNode.getChildWithKeyValue("name", "fonts");
                if (fontsNode == null)
                    JOptionPane.showMessageDialog(this, "Could not find fonts node!");
                else {
                    for (int i=0; i<fontsNode.getChildCount(); i++){
                        SlonNode slonNode = fontsNode.getChildAt(i);
                        FontNode fontNode = new FontNode(slonNode.getKeyValue("name"));
                        // название шрифта и его характеристики
                        fontNode.name = slonNode.getKeyValue("name");
                        fontNode.familyName = slonNode.getKeyValue("family");
                        fontNode.glyphSet = slonNode.getKeyValue("glyphs");
                        fontNode.size = slonNode.getKeyAsInt("size");
                        fontNode.border = slonNode.getKeyAsInt("border");
                        fontNode.outline = slonNode.getKeyAsInt("outline");
                        fontNode.isBold = slonNode.getKeyAsBoolean("bold");
                        fontNode.isItalic = slonNode.getKeyAsBoolean("italic");
                        fontTree.addFont(fontNode);
                    }
                }
            }
            updateList();
        }
        CURRENT_DIR = projectFileChooser.getCurrentDirectory();
    }

    private void cmdFileSave(){
        if (CURRENT_PRJ_FILE != null){
            saveProjectToFile(CURRENT_PRJ_FILE);
        }
        else cmdFileSaveAs();
    }

    private void cmdFileSaveAs(){
        int returnVal = projectFileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = projectFileChooser.getSelectedFile();
            if (file.exists()){
                int confirm = JOptionPane.showConfirmDialog(this, "Owerwrite file "+file.getName()+"?", "Atlas maker", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.NO_OPTION) return;
            }
            saveProjectToFile(file);
        }
    }

    // перезаписывает файл f
    private void saveProjectToFile(File f){
        String  filePath = f.getAbsolutePath();

        String  fileExt = getFileExtension(filePath);
        if (fileExt==null || !fileExt.equalsIgnoreCase("packer")) filePath += ".packer";
        setTitle("["+filePath+"] - Atlas Maker");

        // создадим слон файл
        Slon slon = new Slon();
        SlonNode rootNode = new SlonNode();
        // запишем все параметры
        rootNode.setKeyValue("width", (String)comboWidth.getSelectedItem());
        rootNode.setKeyValue("height", (String)comboHeight.getSelectedItem());
        rootNode.setKeyValue("export", textExportPath.getText());

        // спрайты
        SlonNode spritesNode = new SlonNode();
        spritesNode.setKeyValue("name","sprites");
        for (int i=0; i< spriteTree.getCount(); i++){
            SlonNode sprN = new SlonNode();
            SpriteNode spr = spriteTree.getNodeAt(i);
            // название спрайта и его характеристики
            sprN.setKeyValue("name", spr.toString());
            sprN.setKeyValue("outline", spr.isOutlineWithBorder);
            sprN.setKeyValue("crop", spr.isCropped);
            sprN.setKeyValue("border", spr.transparentBorder);
            for (int j=0; j<spr.getChildCount(); j++){
                SlonNode picN = new SlonNode();
                FrameNode frame = spr.getChildAt(j);
                // запишем имена файлов отдельного кадра и их характеристики
                picN.setKeyValue("file", frame.getPath());
                picN.setKeyValue("pivotX", frame.xPivot);
                picN.setKeyValue("pivotY", frame.yPivot);
                sprN.addChild(picN);
            }
            spritesNode.addChild(sprN);
        }
        rootNode.addChild(spritesNode);

        // шрифты
        SlonNode fontsNode = new SlonNode();
        fontsNode.setKeyValue("name","fonts");
        for (int i=0; i< fontTree.getCount(); i++){
            SlonNode fntN = new SlonNode();
            FontNode fnt =fontTree.getNodeAt(i);
            // название шрифта и его характеристики
            fntN.setKeyValue("name", fnt.toString());
            fntN.setKeyValue("family", fnt.familyName);
            fntN.setKeyValue("glyphs", fnt.glyphSet);
            fntN.setKeyValue("size", fnt.size);
            fntN.setKeyValue("border", fnt.border);
            fntN.setKeyValue("outline", fnt.outline);
            fntN.setKeyValue("bold",fnt.isBold);
            fntN.setKeyValue("italic", fnt.isItalic);
            fontsNode.addChild(fntN);
        }
        rootNode.addChild(fontsNode);

        slon.setRoot(rootNode);
        slon.save(filePath);

        NEED_TO_SAVE = false;
    }

    // выход с запросом сохранения
    private void cmdFileExit(){
        if (NEED_TO_SAVE){
            int answer = JOptionPane.showOptionDialog(this, "Project not saved! Do you want to save it?", "Atlas MMaker",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,null,null);
            if (answer == JOptionPane.YES_OPTION)
                cmdFileSave();
            else if (answer == JOptionPane.CANCEL_OPTION ||answer ==JOptionPane.CLOSED_OPTION)
                return;
        }
        System.exit(0);
    }

//    private void cmdFileExit(ActionEvent e){
//        JOptionPane.showMessageDialog(this,"mnu file exit");
//        System.exit(0);
//    }

    private void cmdPackAtlas(){
        if (textExportPath.getText().equalsIgnoreCase("")){
            JOptionPane.showMessageDialog(this,"Enter the export file name!");
            return;
        }

        String filename = textExportPath.getText();
        int w = Integer.decode((String)comboWidth.getSelectedItem());
        int h = Integer.decode((String)comboHeight.getSelectedItem());

        int nx,ny;

        PackAlgorithm algorithm = new PackAlgorithm(spriteTree, fontTree, w,h);
        String[] fileNames = algorithm.start();
        if (fileNames.length != 0){
            JOptionPane.showMessageDialog(this, "There is not enough room on the atlas. " +fileNames.length+" images not added to atlas!!!" );
        }
        // нарисуем полученный атлас на изображение панели
        atlasPanel.setPreferredSize(new Dimension(w,h));
        atlasPanel.atlasImage = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);

        Graphics atlasGraphics = atlasPanel.atlasImage.getGraphics();

        // нарисуем спрайты картинки в атлас
        for (int i=0; i< spriteTree.getCount(); i++) {
            SpriteNode sprNode = spriteTree.getNodeAt(i);

            for (int j=0; j<sprNode.getChildCount(); j++){
                FrameNode frmNode = sprNode.getChildAt(j);

                if (frmNode.pictureRect.isInAtlas) {
//                    nx = frmNode.pictureRect.x+sprNode.transparentBorder+outlineBorder;
//                    ny = frmNode.pictureRect.y+sprNode.transparentBorder+outlineBorder;

                    atlasGraphics.drawImage(frmNode.resultImage,frmNode.pictureRect.x, frmNode.pictureRect.y, null);
//                    // ЖУТКИЙ КОСТЫЛЬ!!!!
//                    if (sprNode.isCropped)
//                        atlasGraphics.drawImage(frmNode.image, nx - frmNode.cropLeft, ny - frmNode.cropTop, null);
//                    else
//                        atlasGraphics.drawImage(frmNode.image, nx, ny, null);
                }
            }
        }

        // нарисуем шрифты картинки в атлас
        for (int i=0; i< fontTree.getCount(); i++) {
            FontNode fntNode = fontTree.getNodeAt(i);
            for (int j=0; j<fntNode.getChildCount(); j++){
                GlyphNode glphNode = fntNode.getChildAt(j);

                if (glphNode.pictureRect.isInAtlas) {
                    // шрифты содержат картинку уже со всеми отступами и т.д.
                    nx = glphNode.pictureRect.x;//+fntNode.border;
                    ny = glphNode.pictureRect.y;//+fntNode.border;
                    atlasGraphics.drawImage(glphNode.image, nx, ny, null);
                }
            }
        }


        try {
            ImageIO.write(atlasPanel.atlasImage, "png", new File(filename));
            export_atlas(filename+".atlas");
            export_atlas_to_json(filename+".json");
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        atlasPanel.repaint();
        atlasScroll.getViewport().revalidate();
    }



    private void cmdEditSprite(){
        int selind = listSprites.getSelectedIndex();
        if (selind < 0 )
            JOptionPane.showMessageDialog(this, "Select sprite before!");
        else    // запустим диалог редактирования спрайта
            (new EditSpriteDialog(this,selind)).setVisible(true);
    }


    private void cmdEditFont(){
        int selind = listFonts.getSelectedIndex();
        if (selind < 0 )
            JOptionPane.showMessageDialog(this, "Select font before!");
        else    // запустим диалог редактирования спрайта
            (new EditFontDialog(this,selind)).setVisible(true);
    }

    // нажата кнопочка добавления нового шрифта
    private void cmdAddFont(){
        EditFontDialog editFontDialog = new EditFontDialog(this,-1);
        editFontDialog.setVisible(true);
    }

    // нажата кнопочка удаления шрифта
    private void cmdDelFont(){
        int selind = listFonts.getSelectedIndex();
        if (selind < 0 ){
            JOptionPane.showMessageDialog(this, "Select font before!");
            return;
        }

        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure to delete entire font?", "Atlas maker",
                JOptionPane.YES_NO_OPTION)) return;
        fontTree.remove(selind);
        updateList();
        NEED_TO_SAVE = true;
    }


    // нажата кнопочка добавления нового спрайта
    private void cmdAddSprite(){
        EditSpriteDialog editSpriteDialog = new EditSpriteDialog(this,-1);
        editSpriteDialog.setVisible(true);
    }

    // нажата кнопочка удаления спрайта
    private void cmdDelSprite(){
        int selind = listSprites.getSelectedIndex();
        if (selind < 0 ){
            JOptionPane.showMessageDialog(this, "Select sprite before!");
            return;
        }

        if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(this, "Are you sure to delete entire sprite?", "Atlas maker",
                JOptionPane.YES_NO_OPTION)) return;
        spriteTree.remove(selind);
        updateList();
        NEED_TO_SAVE = true;
    }


    // диспетчеризация событий от меню и тулбаров
    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnNew || obj == mnuFileNew) cmdFileNew(e);
        if (obj == btnOpen || obj == mnuFileOpen) cmdFileOpen(e);
        if (obj == btnSave || obj == mnuFileSave) cmdFileSave();
        if (obj == btnPack || obj == mnuAtlasPack) cmdPackAtlas();
        if (obj == mnuFileSaveAs) cmdFileSaveAs();
        if (obj == mnuFileExit) cmdFileExit();
        // команды левого тулбара
        if (obj == btnEditSprite) cmdEditSprite();
        if (obj == btnAddSprite)  cmdAddSprite();
        if (obj == btnDelSprite)  cmdDelSprite();
        // команды левого тулбара
        if (obj == btnEditFont)   cmdEditFont();
        if (obj == btnAddFont)    cmdAddFont();
        if (obj == btnDelFont)    cmdDelFont();
    }

    private void cmdListSpritesClicked(MouseEvent e){
        if (e.getClickCount() == 2){
            cmdEditSprite();
        }
    }

    private void cmdListFontsClicked(MouseEvent e){
        if (e.getClickCount() == 2){
            cmdEditFont();
        }
    }

    // получает расширение файла
    private static String getFileExtension(String fullPath){
        int sepPos = fullPath.lastIndexOf(File.separator);
        String nameAndExt = fullPath.substring(sepPos + 1, fullPath.length());
        int dotPos = nameAndExt.lastIndexOf(".");
        return dotPos!=-1 ? nameAndExt.substring(dotPos + 1):null;
    }

    // экспортитуем полученный атлас в файл
    private void export_atlas(String path){
        Slon slon = new Slon();
        SlonNode rootNode = new SlonNode();
        SlonNode spritesNode = new SlonNode();
        spritesNode.setKeyValue("name", "sprites");

        SlonNode fontsNode = new SlonNode();
        fontsNode.setKeyValue("name","fonts");

        rootNode.setKeyValue("width", (String)comboWidth.getSelectedItem());
        rootNode.setKeyValue("height", (String)comboHeight.getSelectedItem());

        // экспорт спрайтов в секцию со спрайтами
        for (int i=0; i< spriteTree.getCount(); i++){
            SlonNode spriteSlon = new SlonNode();
            SpriteNode sprNode = spriteTree.getNodeAt(i);
            // если есть обводка - будем выгружать с ее учетом
            int outlineBorder = 0;
            if (sprNode.isOutlineWithBorder)
                outlineBorder = 1;
            spriteSlon.setKeyValue("name", sprNode.toString());
            for (int j=0; j<sprNode.getChildCount(); j++){
                SlonNode frameSlon = new SlonNode();
                FrameNode frmNode = sprNode.getChildAt(j);
                // запишем характеристики отдельного кадра
                frameSlon.setKeyValue("x", (frmNode.pictureRect.x + sprNode.transparentBorder+outlineBorder));
                frameSlon.setKeyValue("y", (frmNode.pictureRect.y + sprNode.transparentBorder+outlineBorder));
                frameSlon.setKeyValue("w", (frmNode.pictureRect.w - 2 * sprNode.transparentBorder-2*outlineBorder));
                frameSlon.setKeyValue("h", (frmNode.pictureRect.h - 2 * sprNode.transparentBorder-2*outlineBorder));
                frameSlon.setKeyValue("pivotX", (frmNode.xPivot - frmNode.cropLeft));
                frameSlon.setKeyValue("pivotY", (frmNode.yPivot - frmNode.cropTop));
                spriteSlon.addChild(frameSlon);
            }
            spritesNode.addChild(spriteSlon);
        }
        rootNode.addChild(spritesNode);

        // экспорт шрифтов в секцию со шрифтами
        for (int i=0; i< fontTree.getCount(); i++){
            SlonNode fontSlon = new SlonNode();
            FontNode fntNode = fontTree.getNodeAt(i);
            fontSlon.setKeyValue("name", fntNode.toString());
            // выгрузим каждую буковку
            for (int j=0; j<fntNode.getChildCount(); j++){
                SlonNode glyphSlon = new SlonNode();
                GlyphNode glphNode = fntNode.getChildAt(j);
                glyphSlon.setKeyValue("glyph",glphNode.glyph);
                glyphSlon.setKeyValue("x", (glphNode.pictureRect.x + fntNode.border));
                glyphSlon.setKeyValue("y", (glphNode.pictureRect.y + fntNode.border));
                glyphSlon.setKeyValue("w", (glphNode.pictureRect.w - fntNode.border));
                glyphSlon.setKeyValue("h", (glphNode.pictureRect.h - fntNode.border));

                // расстояние от origin до левой границы изображения буквы. Может быть отрицательным.
                glyphSlon.setKeyValue("lsb", (glphNode.lsb + fntNode.border));
                // расстояние от верха строки до origin символа
                glyphSlon.setKeyValue("originY", (glphNode.originY + fntNode.border));
                // расстояние от origin до origin следующего символа
                glyphSlon.setKeyValue("advance", glphNode.advance);
                fontSlon.addChild(glyphSlon);
            }
            fontsNode.addChild(fontSlon);
        }
        rootNode.addChild(fontsNode);


        slon.setRoot(rootNode);

//	    slon.load("C:\\test.slon");
        slon.save(path);
    }


    // экспортитуем полученный атлас в файл JSON
    private void export_atlas_to_json(String path){
        JSONObject rootJSON = new JSONObject();

        JSONObject fontsJSON = new JSONObject();
        rootJSON.put("fonts", fontsJSON);

        JSONObject spritesJSON = new JSONObject();
        rootJSON.put("sprites", spritesJSON);

        rootJSON.put("height", comboHeight.getSelectedItem());
        rootJSON.put("width", comboWidth.getSelectedItem());

        // экспорт спрайтов в секцию со спрайтами
        for (int i=0; i< spriteTree.getCount(); i++){
            JSONObject spriteJSON = new JSONObject();
            SpriteNode sprNode = spriteTree.getNodeAt(i);
            // если есть обводка - будем выгружать с ее учетом
            int outlineBorder = 0;
            if (sprNode.isOutlineWithBorder)
                outlineBorder = 1;

            JSONArray frames = new JSONArray();
            for (int j=0; j<sprNode.getChildCount(); j++){
                JSONObject frameJSON = new JSONObject();
                FrameNode frmNode = sprNode.getChildAt(j);
                // запишем характеристики отдельного кадра
                frameJSON.put("pivotY", (frmNode.yPivot - frmNode.cropTop));
                frameJSON.put("pivotX", (frmNode.xPivot - frmNode.cropLeft));
                frameJSON.put("h", (frmNode.pictureRect.h - 2 * sprNode.transparentBorder-2*outlineBorder));
                frameJSON.put("w", (frmNode.pictureRect.w - 2 * sprNode.transparentBorder-2*outlineBorder));
                frameJSON.put("y", (frmNode.pictureRect.y + sprNode.transparentBorder+outlineBorder));
                frameJSON.put("x", (frmNode.pictureRect.x + sprNode.transparentBorder+outlineBorder));

                frames.put(frameJSON);
            }
            spriteJSON.put("frames", frames);

            spritesJSON.put(sprNode.toString(), spriteJSON);
        }


        // экспорт шрифтов в секцию со шрифтами
        for (int i=0; i< fontTree.getCount(); i++){
            JSONObject fontJSON = new JSONObject();
            FontNode fntNode = fontTree.getNodeAt(i);

            // выгрузим каждую буковку
            for (int j=0; j<fntNode.getChildCount(); j++){
                JSONObject glyph = new JSONObject();
                GlyphNode glphNode = fntNode.getChildAt(j);
                glyph.put("x", (glphNode.pictureRect.x + fntNode.border));
                glyph.put("y", (glphNode.pictureRect.y + fntNode.border));
                glyph.put("w", (glphNode.pictureRect.w - fntNode.border));
                glyph.put("h", (glphNode.pictureRect.h - fntNode.border));

                // расстояние от origin до левой границы изображения буквы. Может быть отрицательным.
                glyph.put("lsb", (glphNode.lsb + fntNode.border));
                // расстояние от верха строки до origin символа
                glyph.put("originY", (glphNode.originY + fntNode.border));
                // расстояние от origin до origin следующего символа
                glyph.put("advance", glphNode.advance);

                fontJSON.put(glphNode.glyph, glyph);
            }
            fontsJSON.put(fntNode.toString(), fontJSON);
        }


        // сохраним в файл
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(rootJSON.toString(2));
            writer.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save to "+path, "Atlas Maker", JOptionPane.ERROR_MESSAGE);
        }

    }

}

