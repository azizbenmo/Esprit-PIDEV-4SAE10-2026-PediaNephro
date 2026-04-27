package tn.example.events.Services;


import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;
import tn.example.events.Entities.Inscription;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificatService {

    private static final DeviceRgb BLUE_PRIMARY = new DeviceRgb(15, 55, 120);
    private static final DeviceRgb BLUE_LIGHT   = new DeviceRgb(74, 144, 226);
    private static final DeviceRgb GOLD         = new DeviceRgb(184, 148, 50);
    private static final DeviceRgb GOLD_LIGHT   = new DeviceRgb(212, 175, 55);
    private static final DeviceRgb GRAY_TEXT    = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb GRAY_LIGHT   = new DeviceRgb(241, 245, 249);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);

    public byte[] generateCertificat(Inscription inscription) throws Exception {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer     = new PdfWriter(outputStream);
        PdfDocument pdf      = new PdfDocument(writer);

        // ✅ Format paysage A4
        Document document = new Document(pdf, PageSize.A4.rotate());
        document.setMargins(0, 0, 0, 0);

        PdfFont fontBold    = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontItalic  = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        String nomParticipant = inscription.getParticipant().getPrenom()
                + " " + inscription.getParticipant().getNom();
        String nomEvent  = inscription.getEvent().getNomEvent();
        String dateEvent = inscription.getEvent().getDateDebut()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        String lieu      = inscription.getEvent().getLieu();
        String today     = java.time.LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // =============================================
        // ✅ BORDURE EXTERNE DORÉE
        // =============================================
        Table borderTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(new SolidBorder(GOLD, 8))
                .setMargin(0)
                .setPadding(0);

        // =============================================
        // ✅ HEADER — fond bleu foncé
        // =============================================
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 3, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setBackgroundColor(BLUE_PRIMARY);

        // Logo à gauche
        try {
            InputStream logoStream = getClass()
                    .getResourceAsStream("/static/logo.png");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                Image logo = new Image(ImageDataFactory.create(logoBytes))
                        .setWidth(100)
                        .setHeight(90);
                Cell logoCell = new Cell()
                        .add(logo)
                        .setBorder(Border.NO_BORDER)
                        .setPadding(15)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
                header.addCell(logoCell);
            } else {
                // Fallback texte si pas de logo
                Cell logoCell = new Cell()
                        .add(new Paragraph("🏥")
                                .setFontSize(30)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(Border.NO_BORDER)
                        .setPadding(15);
                header.addCell(logoCell);
            }
        } catch (Exception e) {
            Cell logoCell = new Cell()
                    .add(new Paragraph("PédiaNéphro")
                            .setFont(fontBold)
                            .setFontSize(12)
                            .setFontColor(ColorConstants.WHITE))
                    .setBorder(Border.NO_BORDER)
                    .setPadding(15);
            header.addCell(logoCell);
        }

        // Titre centre
        Cell titleCell = new Cell()
                .add(new Paragraph("CERTIFICAT DE PARTICIPATION")
                        .setFont(fontBold)
                        .setFontSize(22)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setCharacterSpacing(3))
                .add(new Paragraph("Centre de Néphrologie Pédiatrique — PédiaNéphro")
                        .setFont(fontItalic)
                        .setFontSize(10)
                        .setFontColor(new DeviceRgb(200, 220, 255))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(titleCell);

        // Numéro droite
        Cell numCell = new Cell()
                .add(new Paragraph("N° " + String.format("%04d",
                        inscription.getIdInscription()))
                        .setFont(fontRegular)
                        .setFontSize(10)
                        .setFontColor(new DeviceRgb(200, 220, 255))
                        .setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(today)
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(new DeviceRgb(200, 220, 255))
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        header.addCell(numCell);

        document.add(header);

        // =============================================
        // ✅ LIGNE DORÉE
        // =============================================
        LineSeparator goldLine = new LineSeparator(new SolidLine(4));
        goldLine.setStrokeColor(GOLD_LIGHT);
        goldLine.setWidth(UnitValue.createPercentValue(100));
        document.add(goldLine);

        // =============================================
        // ✅ CORPS PRINCIPAL
        // =============================================
        document.add(new Paragraph(" ").setMarginBottom(15));

        // Sous-titre
        document.add(new Paragraph("Il est certifié que")
                .setFont(fontItalic)
                .setFontSize(14)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        // ✅ Nom du participant — très grand
        document.add(new Paragraph(nomParticipant.toUpperCase())
                .setFont(fontBold)
                .setFontSize(36)
                .setFontColor(BLUE_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setCharacterSpacing(2)
                .setMarginBottom(5));

        // Ligne dorée sous le nom
        LineSeparator nameLine = new LineSeparator(new SolidLine(1));
        nameLine.setStrokeColor(GOLD);
        nameLine.setWidth(UnitValue.createPercentValue(40));
        nameLine.setHorizontalAlignment(HorizontalAlignment.CENTER);
        document.add(nameLine);

        document.add(new Paragraph(" ").setMarginBottom(10));

        // Type participant
        String typeLabel = inscription.getTypeParticipant() != null &&
                "PROFESSIONNEL".equals(inscription.getTypeParticipant().name())
                ? "Professionnel de Santé" : "Participant";

        document.add(new Paragraph(typeLabel)
                .setFont(fontItalic)
                .setFontSize(13)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Texte participation
        document.add(new Paragraph("a participé avec succès à l'événement")
                .setFont(fontRegular)
                .setFontSize(13)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(8));

        // ✅ Nom de l'événement dans un encadré doré
        Table eventBox = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(70))
                .setHorizontalAlignment(HorizontalAlignment.CENTER)
                .setBorder(new SolidBorder(GOLD, 2))
                .setBackgroundColor(GRAY_LIGHT)
                .setMarginBottom(15);

        eventBox.addCell(new Cell()
                .add(new Paragraph("« " + nomEvent + " »")
                        .setFont(fontBold)
                        .setFontSize(16)
                        .setFontColor(BLUE_PRIMARY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("📅 " + dateEvent + "   📍 " + lieu)
                        .setFont(fontRegular)
                        .setFontSize(11)
                        .setFontColor(GRAY_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPadding(12));

        document.add(eventBox);

        // =============================================
        // ✅ FOOTER — signature + cachet
        // =============================================
        document.add(new Paragraph(" ").setMarginBottom(10));

        Table footer = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMarginTop(10);

        // Colonne gauche — cachet
        Cell cachetCell = new Cell()
                .add(new Paragraph("Cachet de l'établissement")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(BLUE_PRIMARY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("\n\n\n")
                        .setHeight(55)
                        .setBorder(new SolidBorder(GRAY_TEXT, 1)))
                .setBorder(Border.NO_BORDER)
                .setPadding(15);
        footer.addCell(cachetCell);

        // Colonne centre — validité
        Cell validiteCell = new Cell()
                .add(new Paragraph("✓ Certificat Officiel")
                        .setFont(fontBold)
                        .setFontSize(11)
                        .setFontColor(GOLD)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Délivré le " + today)
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(GRAY_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Référence : CERT-" +
                        inscription.getIdInscription() + "-" +
                        java.time.LocalDate.now().getYear())
                        .setFont(fontRegular)
                        .setFontSize(9)
                        .setFontColor(GRAY_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(GOLD, 1))
                .setBackgroundColor(GRAY_LIGHT)
                .setPadding(15)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        footer.addCell(validiteCell);

        // Colonne droite — signature directeur
        Cell signatureCell = new Cell()
                .add(new Paragraph("Le Directeur Médical")
                        .setFont(fontBold)
                        .setFontSize(10)
                        .setFontColor(BLUE_PRIMARY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("\n\n")
                        .setHeight(40)
                        .setBorderBottom(new SolidBorder(BLUE_PRIMARY, 1)))
                .add(new Paragraph("Dr. [Nom Directeur]")
                        .setFont(fontItalic)
                        .setFontSize(10)
                        .setFontColor(GRAY_TEXT)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPadding(15);
        footer.addCell(signatureCell);

        document.add(footer);

        // =============================================
        // ✅ BAS DE PAGE
        // =============================================
        LineSeparator footerLine = new LineSeparator(new SolidLine(1));
        footerLine.setStrokeColor(BLUE_LIGHT);
        document.add(footerLine);

        document.add(new Paragraph(
                "PédiaNéphro — Centre de Néphrologie Pédiatrique  |  " +
                        "Ce document est généré automatiquement et fait foi")
                .setFont(fontRegular)
                .setFontSize(8)
                .setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5)
                .setMarginBottom(10));

        document.close();
        return outputStream.toByteArray();
    }
}