import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class DocumentProcessingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}

/* =========================
   PRODUCT
   ========================= */

interface DocumentProcessor {
    String process(String country, String format) throws Exception;
}

/* =========================
   CONCRETE PRODUCTS
   ========================= */

class InvoiceProcessor implements DocumentProcessor {

    public String process(String country, String format) throws Exception {

        if(!format.equals("pdf") && !format.equals("csv") && !format.equals("txt"))
            throw new Exception("Formato invalido para factura");

        return "Factura electronica procesada para " + country;
    }
}

class ContractProcessor implements DocumentProcessor {

    public String process(String country, String format) throws Exception {

        if(!format.equals("doc") && !format.equals("docx") && !format.equals("pdf") && !format.equals("txt"))
            throw new Exception("Formato invalido para contrato");

        return "Contrato legal procesado para " + country;
    }
}

class FinancialReportProcessor implements DocumentProcessor {

    public String process(String country, String format) throws Exception {

        if(!format.equals("xlsx") && !format.equals("csv") && !format.equals("txt"))
            throw new Exception("Formato invalido para reporte financiero");

        return "Reporte financiero procesado para " + country;
    }
}

class CertificateProcessor implements DocumentProcessor {

    public String process(String country, String format) throws Exception {

        if(!format.equals("pdf") && !format.equals("txt"))
            throw new Exception("Formato invalido para certificado");

        return "Certificado digital procesado para " + country;
    }
}

class TaxDeclarationProcessor implements DocumentProcessor {

    public String process(String country, String format) throws Exception {

        if(!format.equals("pdf") && !format.equals("csv") && !format.equals("txt"))
            throw new Exception("Formato invalido para declaracion tributaria");

        return "Declaracion tributaria procesada para " + country;
    }
}

/* =========================
   CREATOR
   ========================= */

abstract class DocumentFactory {
    public abstract DocumentProcessor createProcessor();
}

/* =========================
   CONCRETE CREATORS
   ========================= */

class InvoiceFactory extends DocumentFactory {
    public DocumentProcessor createProcessor() {
        return new InvoiceProcessor();
    }
}

class ContractFactory extends DocumentFactory {
    public DocumentProcessor createProcessor() {
        return new ContractProcessor();
    }
}

class FinancialReportFactory extends DocumentFactory {
    public DocumentProcessor createProcessor() {
        return new FinancialReportProcessor();
    }
}

class CertificateFactory extends DocumentFactory {
    public DocumentProcessor createProcessor() {
        return new CertificateProcessor();
    }
}

class TaxDeclarationFactory extends DocumentFactory {
    public DocumentProcessor createProcessor() {
        return new TaxDeclarationProcessor();
    }
}

/* =========================
   USER INTERFACE
   ========================= */

class MainWindow extends JFrame {

    private JComboBox<String> countryBox;
    private JComboBox<String> documentBox;
    private JTextArea outputArea;

    private List<String[]> batch = new ArrayList<>();

    public MainWindow(){

        setTitle("Sistema de procesamiento de documentos");
        setSize(720,540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildInterface();

        setVisible(true);
    }

    private void buildInterface(){

        Color bluePrimary = new Color(25,118,210);
        Color blueLight = new Color(227,242,253);

        JPanel topPanel = new JPanel(new GridLayout(4,2,10,10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        topPanel.setBackground(blueLight);

        JLabel countryLabel = new JLabel("Pais");
        JLabel documentLabel = new JLabel("Tipo de documento");

        countryBox = new JComboBox<>(new String[]{
                "Colombia","Mexico","Argentina","Chile"
        });

        documentBox = new JComboBox<>(new String[]{
                "Auto detectar",
                "Factura electronica",
                "Contrato legal",
                "Reporte financiero",
                "Certificado digital",
                "Declaracion tributaria"
        });

        JButton uploadButton = new JButton("Subir archivos");
        JButton processButton = new JButton("Procesar lote");

        uploadButton.setBackground(bluePrimary);
        uploadButton.setForeground(Color.WHITE);

        processButton.setBackground(new Color(13,71,161));
        processButton.setForeground(Color.WHITE);

        topPanel.add(countryLabel);
        topPanel.add(countryBox);

        topPanel.add(documentLabel);
        topPanel.add(documentBox);

        topPanel.add(uploadButton);
        topPanel.add(processButton);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas",Font.PLAIN,14));

        JScrollPane scroll = new JScrollPane(outputArea);

        add(topPanel,BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);

        uploadButton.addActionListener(e -> uploadFiles());
        processButton.addActionListener(this::processBatch);
    }

    /* =========================
       FILE READER
       ========================= */

    private String readFileContent(File file){

        StringBuilder content = new StringBuilder();

        try{

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            int limit = 5;

            while((line = reader.readLine()) != null && limit > 0){
                content.append(line).append("\n");
                limit--;
            }

            reader.close();

        }catch(Exception e){
            return "";
        }

        return content.toString().toLowerCase();
    }

    /* =========================
       AUTO DOCUMENT DETECTION
       ========================= */

    private String detectDocumentType(String content){

        if(content.contains("factura"))
            return "Factura electronica";

        if(content.contains("contrato"))
            return "Contrato legal";

        if(content.contains("reporte"))
            return "Reporte financiero";

        if(content.contains("certificado"))
            return "Certificado digital";

        if(content.contains("impuesto") || content.contains("tributaria"))
            return "Declaracion tributaria";

        return "Factura electronica";
    }

    /* =========================
       FILE UPLOAD
       ========================= */

    private void uploadFiles(){

        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);

        int result = chooser.showOpenDialog(this);

        if(result == JFileChooser.APPROVE_OPTION){

            File[] files = chooser.getSelectedFiles();

            for(File file : files){

                String name = file.getName();
                String extension = "";

                int i = name.lastIndexOf('.');

                if(i > 0)
                    extension = name.substring(i+1).toLowerCase();

                String country = (String) countryBox.getSelectedItem();
                String document = (String) documentBox.getSelectedItem();

                String content = readFileContent(file);

                if(document.equals("Auto detectar") && !content.isEmpty()){
                    document = detectDocumentType(content);
                }

                batch.add(new String[]{country,document,extension});

                outputArea.append("Archivo agregado: " + name + "\n");

                if(!content.isEmpty()){
                    outputArea.append("Contenido detectado:\n");
                    outputArea.append(content + "\n");
                }

                outputArea.append("Tipo detectado: " + document + "\n\n");
            }
        }
    }

    /* =========================
       PROCESS BATCH
       ========================= */

    private void processBatch(ActionEvent e){

        outputArea.append("\nProcesando lote...\n\n");

        for(String[] item : batch){

            try{

                DocumentFactory factory = getFactory(item[1]);

                DocumentProcessor processor =
                        factory.createProcessor();

                String result =
                        processor.process(item[0], item[2]);

                outputArea.append("OK -> " + result + "\n");

            }catch(Exception ex){

                outputArea.append("ERROR -> " + ex.getMessage() + "\n");

            }
        }

        batch.clear();

        outputArea.append("\nLote finalizado\n\n");
    }

    /* =========================
       FACTORY SELECTOR
       ========================= */

    private DocumentFactory getFactory(String type){

        switch(type){

            case "Factura electronica":
                return new InvoiceFactory();

            case "Contrato legal":
                return new ContractFactory();

            case "Reporte financiero":
                return new FinancialReportFactory();

            case "Certificado digital":
                return new CertificateFactory();

            case "Declaracion tributaria":
                return new TaxDeclarationFactory();

            default:
                return new InvoiceFactory();
        }
    }
}