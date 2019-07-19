package com.richur.www;

import com.sun.xml.internal.fastinfoset.Encoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: liu lei
 * @Date: 2019/6/16
 */
public class DownloadPicture extends JFrame implements ActionListener {
    private static Logger log = LogManager.getLogger("COMMON");

    private JTextArea ta;
    private JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

    private JButton bOpen, bSave;
    private JScrollPane ps;
    private JProgressBar jpb;

    public DownloadPicture() {
        ta = new JTextArea(10, 40);
        ta.setWrapStyleWord(true); //换行方式：不分割单词
        ta.setLineWrap(true); //自动换行
        //ta.setEnabled(false);
        ps = new JScrollPane(ta, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bOpen = new JButton("选择文件");
        bSave = new JButton("下载照片");

        bOpen.addActionListener(this);
        bSave.addActionListener(this);
        jpb = new JProgressBar();
        jpb.setMinimum(0);
        jpb.setStringPainted(true);
        //jpb.setLocation(12,12);

        this.add(ps);
        this.add(bOpen);
        this.add(bSave);
        this.add(jpb);
        bSave.setEnabled(false);
        this.setTitle("员工工牌照片下载");
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        this.setSize(520, 285);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        jfc.setDialogTitle("选择图片");
        jfc.setAcceptAllFileFilterUsed(false);
        //限制文件只能显示PNG\JPG格式的图片
        FileNameExtensionFilter filter = new FileNameExtensionFilter(".CSV文件", "csv");
        jfc.addChoosableFileFilter(filter);
    }

    public static void main(String[] args) {

        DownloadPicture fc = new DownloadPicture();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JButton jbt = (JButton) ae.getSource();
        if (jbt == bOpen) {
            ta.setText("");
            //打开文件选择器对话框
            int status = jfc.showOpenDialog(this);

            if (status != JFileChooser.APPROVE_OPTION) {
                ta.setText("没有选中文件\n");
                bSave.setEnabled(false);
            } else {
                try {
                    //被选中的文件保存为文件对象
                    File file = jfc.getSelectedFile();
                    //把读取的目录存到文本框中
                    File absoluteFile = file.getAbsoluteFile();
                    ta.setText(absoluteFile.getPath());
                } catch (Exception e) {
                    System.out.println("\n系统没有找到此文件");
                    log.error("系统没有找到此文件", e);
                }
                bSave.setEnabled(true);
            }
        }
        if (jbt == bSave) {
            String text = ta.getText();
            String commonPath = "employee" + LocalDate.now().toString() + File.separator;
            File file = new File(commonPath);
            if (!file.exists() && !file.isDirectory()) {
                file.mkdir();
                log.info("创建目录:[{}]", commonPath);
            }
            if (text.endsWith(".csv")) {
                List<EmployeePic> employeePicList = readCVS(text);
                if(null == employeePicList || employeePicList.size() == 0){
                    log.info("[{}]内容为空，无图片可以下载", text.substring(text.lastIndexOf(File.separator) + 1));
                    ta.append("\n" + text.substring(text.lastIndexOf(File.separator) + 1) + "内容为空，无图片可以下载");
                }
                int len = employeePicList.size();
                jpb.setMaximum(len);
                int i = 1;
                int noPicture = 0;
                int hasPicture = 0;
                Dimension d = jpb.getSize();
                Rectangle rect = new Rectangle(0, 0, d.width, d.height);
                for (EmployeePic employeePic : employeePicList) {
                    ta.paintImmediately(ta.getBounds());
                    String name = employeePic.name;
                    String workNum = employeePic.workNum;
                    String downloadPath = null;
                    if(employeePic.picFlag == 7){
                        if(null == name || "".equals(name)){
                            name = "无姓名";
                        }
                        if(null == workNum || "".equals(workNum)){
                            workNum = "无工号";
                        }
                        downloadPath = commonPath + name + workNum + ".jpg";
                        downloadPicture(employeePic.picUrl, downloadPath);
                        hasPicture ++;

                    }
                    if(null == downloadPath){
                        noPicture ++;
                        downloadPath = employeePic.name + employeePic.workNum;
                        log.info(downloadPath + "-无照片，总进度：" + (i * 100 / len) + "%");
                        ta.append("\n" + downloadPath + "-无照片，总进度：" + (i * 100 / len) + "%");
                    } else{
                        log.info(downloadPath + "下载完成，总进度：" + (i * 100 / len) + "%");
                        ta.append("\n" + downloadPath + "下载完成，总进度：" + (i * 100 / len) + "%");
                    }
                    //Point p = new Point();
                    //p.setLocation(0, ta.getLineCount()*5);
                    //System.out.println(ta.getLineCount());
                    //ps.getViewport().setViewPosition(p);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        log.error( ex);
                    }
                    ps.getVerticalScrollBar().setValue(ps.getVerticalScrollBar().getMaximum());
                    //ta.paintImmediately(ta.getBounds());
                    jpb.setValue(i);
                    jpb.paintImmediately(rect);
                    i++;
                }
                i --;
                ta.append("\n一共入职["+ i +"]人，下载照片["+ hasPicture +"]张，无照片["+ noPicture +"]人");
                log.info("一共入职["+ i +"]人，下载照片["+ hasPicture +"]张，无照片["+ noPicture +"]人");
            } else {
                ta.append("\n下载失败");
                log.error("文件并不是csv文件:[{}]", text);
            }
            ta.paintImmediately(ta.getBounds());
            bSave.setEnabled(false);
        }

    }

    private long downloadPicture(String urlStr, String path) {
        long fileSize = 0L;
        try {
            URL url = new URL(urlStr);
            DataInputStream dataInputStream = new DataInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            //文件大小
            fileSize = output.size();
            fileOutputStream.write(output.toByteArray());
            dataInputStream.close();
            fileOutputStream.close();
            log.info("下载文件[{}],大小:[{}]MB", path,fileSize/1000000.0);
        } catch (MalformedURLException e) {
            log.error("downloadPicture-MalformedURLException", e);
        } catch (IOException e) {
            log.error("downloadPicture-IOException", e);
        }
        return fileSize;
    }

    private List<EmployeePic> readCVS(String path) {
        File csv = new File(path);  // CSV文件路径
        BufferedReader br = null;
        List<EmployeePic> employeePicList = new ArrayList<>();
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(csv));
            br= new BufferedReader(new InputStreamReader(in, Encoder.UTF_8));
            //br = new BufferedReader(new FileReader(csv));
        } catch (FileNotFoundException e) {
            log.error("readCVS-FileNotFoundException", e);
        } catch (UnsupportedEncodingException e) {
            log.error("readCVS-UnsupportedEncodingException", e);
        }
        String line = "";
        String everyLine = "";
        try {
            assert br != null;
            //读取到的内容给line变量
            while ((line = br.readLine()) != null) {
                try {
                    everyLine = line;
                    if(!"".equals(everyLine.trim())) {
                        EmployeePic employeePic = new EmployeePic();
                        String[] str = everyLine.split(";");
                        if (str.length == 3) {
                            employeePic.name = str[0];
                            employeePic.workNum = str[1];
                            employeePic.picUrl = str[2];
                            employeePic.picFlag = 7;
                            employeePicList.add(employeePic);
                        } else if (str.length == 2) {
                            employeePic.name = str[0];
                            employeePic.workNum = str[1];
                            employeePic.picFlag = 3;
                            employeePicList.add(employeePic);
                        } else if (str.length == 1) {
                            employeePic.name = str[0];
                            employeePic.picFlag = 1;
                            employeePicList.add(employeePic);
                        } else if (str.length == 0) {
                            employeePic.picFlag = 0;
                            employeePicList.add(employeePic);
                        }
                    }
                } catch (Exception e) {
                    log.error("readCVS-Exception", e);
                }
            }
        } catch (IOException e) {
            log.error("readCVS-IOException", e);
        }
        return employeePicList;
    }
}

class EmployeePic {
    String name;
    String workNum;
    String picUrl;
    String depart;
    int picFlag;
}
