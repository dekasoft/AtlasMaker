package com.dekagames.atlasmaker;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;

public class EditFontDialog extends JDialog implements ActionListener {
    private final static String DEFAULT_STRING = "1. Quick red fox jumped over Lazy brown dog...";

    // символ # отсутствует, так как в slon файле, в который выгружается атлас, этот символ является
    // символом комментария, что приводит к ошибкам
    private final static String DEFAULT_GLYPH_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"+
                                                    "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"+
                                                    "#~0123456789.,()-+*//@%!?:;";

    private     BufferedImage   image;              // картинка с текстом
    private     String          strGlyphSet;        // набор символов для генерации
    private     JSplitPane      splitPane;
    private     JButton         btnOk, btnCancel;   // стандартные кнопки диалога
    private     JPanel          leftPanel;          // панель с настройками спрайта и кадра
    private     JPanel          rightPanel;         // панель с изображением кадра
    private     JPanel          leftToolbarPanel;   // тулбар в виде панели над левой панелью
    private     JComboBox       fontCombo;          // комбо с оступными системными шрифтами
    private     JPanel          rightToolbarPanel;  // то же над правой панелью
    private     JTextField      textFontName;       // поле с именем шрифта
    private     JTextArea       textGlyphs;         // поле с набором символов
    private     JSpinner        borderSpin;         // спиннер прозрачной границы вокруг спрайта
    private     JSpinner        sizeSpin;           // спиннер размера шрифта
    private     JSpinner        outlineSpin;        // спиннер размера обводки
    private     JCheckBox       checkBold, checkItalic; // жирный и курсив


    private     DrawFramePanel  drawFramePanel;     // своя панель для изображения кадра спрайта
    private     JScrollPane     frameScroll;        // скролл панель для картинки

    private     int             fontIndex;
    private     boolean         bCreateNew;
    private     FontNode        origFont;           // оригинальная копия - ее перезапишем, если нажмем кнопку Ок
    private     FontNode        fontNode;           // локальная копия - с ней будем работать


    public EditFontDialog(Frame parent, int index){
        super(parent, true);        // модальное окно
        setSize(900,600);
        setLocation(100,80);
        setTitle("Edit font");
        initControls();

        fontIndex = index;
        if (index <0) {       // создаем новый спрайт
            bCreateNew = true;
            String name = "font-"+MainApplication.mainWindow.fontTree.getCount();
            fontNode = new FontNode(name);
            textFontName.setText(name);           // напишем имя в строке редактирования
            strGlyphSet = DEFAULT_GLYPH_SET;
        }
        else {                      // редактируем старый
            origFont = MainApplication.mainWindow.fontTree.getNodeAt(index);
            bCreateNew = false;
            fontNode = new FontNode(origFont);
            textFontName.setText(fontNode.toString());
            strGlyphSet = fontNode.glyphSet;
            int idx = find_font_in_combo(fontNode.familyName);
            if (idx<0) {
                JOptionPane.showMessageDialog(this, "Could not find font "+fontNode.familyName+"!. Set to default.");
                idx = 0;
            }
            fontCombo.setSelectedIndex(idx);
        }

        // выставим настройки на форме
        textGlyphs.setText(strGlyphSet);
        borderSpin.setValue(fontNode.border);
        outlineSpin.setValue(fontNode.outline);
        sizeSpin.setValue(fontNode.size);
        checkBold.setSelected(fontNode.isBold);
        checkItalic.setSelected(fontNode.isItalic);

        redraw_panel();
    }

    // ищет шрифт с именем family_name в комбобоксе системных шрифтов и возвращает его индекс или -1 если шрифт не найден
    private int find_font_in_combo(String family_name){
        for (int i=0; i<fontCombo.getItemCount(); i++){
            if (fontCombo.getItemAt(i).toString().equalsIgnoreCase(family_name))
                return i;
        }
        return -1;
    }

    private void initControls(){
        setLayout(new BorderLayout());
        // стандартные кнопки (как в книжке портянкина)
        btnOk = new JButton("Ok");          btnOk.addActionListener(this);
        btnCancel = new JButton("Cancel");  btnCancel.addActionListener(this);

        JPanel grid = new JPanel(new GridLayout(1,2,5,0));
        grid.add(btnOk);
        grid.add(btnCancel);
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flow.add(grid);
        add(flow, BorderLayout.SOUTH);



        // создадим левую панель
        leftPanel = new JPanel(new BorderLayout());
        leftToolbarPanel = new JPanel(new MigLayout("wrap 4"));
        // поле с именем спрайта
        textFontName = new JTextField(5000);
        // комбобокс со списком доступных шритов сразу заполненный
        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String envfonts[] = gEnv.getAvailableFontFamilyNames();
        fontCombo = new JComboBox(envfonts);
        fontCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redraw_panel();
            }
        });
        // текстовое поле с набором символов
        textGlyphs = new JTextArea(10,5000);
        textGlyphs.setLineWrap(true);
        // спиннеры с размером шрифта и толщиной обводки (0 - обводка отсутствует)
        sizeSpin = new JSpinner(new SpinnerNumberModel(20,1,300,1));
        sizeSpin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                redraw_panel();
            }
        });

        outlineSpin = new JSpinner(new SpinnerNumberModel(3,0,300,1));
        outlineSpin.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                redraw_panel();
            }
        });
        borderSpin = new JSpinner(new SpinnerNumberModel(0,0,100,1));

        // чекбоксы жирности и курсивности
        checkBold = new JCheckBox("Bold");
        checkBold.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                redraw_panel();
            }
        });
        checkItalic = new JCheckBox("Italic");
        checkItalic.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                redraw_panel();
            }
        });

        // добавим все в панель
        leftToolbarPanel.add(new JLabel("Font name:"), "span 4");
        leftToolbarPanel.add(textFontName, " span 4");

        leftToolbarPanel.add(new JLabel("Font:"), "span 4");
        leftToolbarPanel.add(fontCombo,"span 4");

        leftToolbarPanel.add(new JLabel("Glyph set:"), "span 4");
        leftToolbarPanel.add(new JScrollPane(textGlyphs),"span 4");

        leftToolbarPanel.add(new JLabel("Font size:"), "span 3");
        leftToolbarPanel.add(sizeSpin);

        leftToolbarPanel.add(new JLabel("Outline thickness:"), "span 3");
        leftToolbarPanel.add(outlineSpin);

        leftToolbarPanel.add(new JLabel("Transparent border:"), "span 3");
        leftToolbarPanel.add(borderSpin);

        leftToolbarPanel.add(checkBold);
        leftToolbarPanel.add(checkItalic);

        leftPanel.add(leftToolbarPanel,BorderLayout.CENTER);

        // правая панель
        rightPanel = new JPanel(new BorderLayout());
        rightToolbarPanel = new JPanel(new MigLayout());

        // панель для прорисовки кадра
        drawFramePanel = new DrawFramePanel();
        drawFramePanel.yPivot = -100;   // чтобы не рисовать перекрестие
        drawFramePanel.yPivot = -100;

        frameScroll = new JScrollPane(drawFramePanel);
        frameScroll.setAutoscrolls(true);

        rightPanel.add(rightToolbarPanel, BorderLayout.NORTH);
        rightPanel.add(frameScroll, BorderLayout.CENTER);

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        add(splitPane,BorderLayout.CENTER);

    }

    // перерисовка панели выбранным кадром
    private void redraw_panel(){
        int fontStyle=Font.PLAIN;
        if (checkBold.isSelected())
            fontStyle = Font.BOLD;
        if (checkItalic.isSelected())
            fontStyle |= Font.ITALIC;

        image = new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = image.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        gr.setBackground(Color.DARK_GRAY);
        gr.clearRect(0,0,1024,1024);


        Font font = new Font(fontCombo.getSelectedItem().toString(), fontStyle, (Integer)sizeSpin.getValue());
        gr.setFont(font);


        FontMetrics fontMetrics = gr.getFontMetrics();
//        int stringWidth = fontMetrics.stringWidth(DEFAULT_STRING);
        int stringHeight = fontMetrics.getHeight();//.getAscent();

        GlyphVector gv = font.createGlyphVector(gr.getFontRenderContext(), DEFAULT_STRING);

        // рисуем сам символ белым цветом
        Shape shapeFilled = gv.getOutline();
        gr.translate(0, stringHeight);
        gr.setPaint(Color.white);
        gr.fill(shapeFilled);
        gr.draw(shapeFilled);

        // если есть обводка, то создаем ее и рисуем черным цветом
        int outlineWidth = (Integer)outlineSpin.getValue();//Float.parseFloat((String)v);//.floatValue();//d.floatValue();
        if (outlineWidth > 0){
            Stroke stroke = new BasicStroke(outlineWidth/FontNode.OUTLINE_FACTOR);
            Shape shapeOutline = stroke.createStrokedShape(shapeFilled);//gv.getOutline();
            gr.setPaint(Color.black);
            gr.fill(shapeOutline);
            gr.draw(shapeOutline);
        }

        drawFramePanel.bufImage = image;
        drawFramePanel.repaint();
        frameScroll.getViewport().revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();
        if (obj == btnCancel) setVisible(false);
        if (obj == btnOk)   cmdOkButton();
    }

    // нажата кнопка ok
    private void cmdOkButton(){
        // новое имя
        fontNode.setName(textFontName.getText());
        // настройки шрифта
        fontNode.familyName = fontCombo.getSelectedItem().toString();
        fontNode.size = (Integer)sizeSpin.getValue();
        fontNode.outline = (Integer)outlineSpin.getValue();
        fontNode.isBold = checkBold.isSelected();
        fontNode.isItalic = checkItalic.isSelected();
        fontNode.border = (Integer)borderSpin.getValue();
        strGlyphSet = textGlyphs.getText();
        fontNode.glyphSet = strGlyphSet;

        // сохраним в дереве или перезапишем
        if (bCreateNew)     MainApplication.mainWindow.fontTree.addFont(fontNode);
        else		        MainApplication.mainWindow.fontTree.replaceNode(fontIndex, fontNode);

        MainApplication.mainWindow.updateList();
        this.setVisible(false);
        MainWindow.NEED_TO_SAVE = true;
    }


}
