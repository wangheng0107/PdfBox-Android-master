/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.visible;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.tom_roush.harmony.awt.geom.AffineTransform;
import com.tom_roush.pdfbox.cos.COSArray;
import com.tom_roush.pdfbox.cos.COSDictionary;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDResources;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.common.PDStream;
import com.tom_roush.pdfbox.pdmodel.graphics.form.PDFormXObject;
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDAcroForm;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDField;
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDSignatureField;

/**
 * Implementation of PDFTemplateBuilder.
 * @see PDFTemplateBuilder
 * @author Vakhtang Koroghlishvili
 */
public class PDVisibleSigBuilder implements PDFTemplateBuilder
{
    private final PDFTemplateStructure pdfStructure;

    @Override
    public void createPage(PDVisibleSignDesigner properties)
    {
        PDPage page = new PDPage(
            new PDRectangle(properties.getPageWidth(), properties.getPageHeight()));
        pdfStructure.setPage(page);
        Log.i("PdfBox-Android", "PDF page has been created");
    }

    @Override
    public void createTemplate(PDPage page) throws IOException
    {
        PDDocument template = new PDDocument();
        template.addPage(page);
        pdfStructure.setTemplate(template);
    }

    public PDVisibleSigBuilder()
    {
        pdfStructure = new PDFTemplateStructure();
        Log.i("PdfBox-Android", "PDF Strucure has been created");
    }

    @Override
    public void createAcroForm(PDDocument template)
    {
        PDAcroForm theAcroForm = new PDAcroForm(template);
        template.getDocumentCatalog().setAcroForm(theAcroForm);
        pdfStructure.setAcroForm(theAcroForm);
        Log.i("PdfBox-Android", "AcroForm page has been created");
    }

    @Override
    public PDFTemplateStructure getStructure()
    {
        return pdfStructure;
    }

    @Override
    public void createSignatureField(PDAcroForm acroForm) throws IOException
    {
        PDSignatureField sf = new PDSignatureField(acroForm);
        pdfStructure.setSignatureField(sf);
        Log.i("PdfBox-Android", "Signature field has been created");
    }

    @Override
    public void createSignature(PDSignatureField pdSignatureField, PDPage page,
                                String signatureName) throws IOException
    {
        PDSignature pdSignature = new PDSignature();
        PDAnnotationWidget widget = pdSignatureField.getWidgets().get(0);
        pdSignatureField.setValue(pdSignature);
        widget.setPage(page);
        page.getAnnotations().add(widget);
        pdSignature.setName(signatureName);
        pdSignature.setByteRange(new int[] { 0, 0, 0, 0 });
        pdSignature.setContents(new byte[4096]);
        pdfStructure.setPdSignature(pdSignature);
        Log.i("PdfBox-Android", "PDSignature has been created");
    }

    @Override
    public void createAcroFormDictionary(PDAcroForm acroForm, PDSignatureField signatureField)
            throws IOException
    {
        @SuppressWarnings("unchecked")
        List<PDField> acroFormFields = acroForm.getFields();
        COSDictionary acroFormDict = acroForm.getCOSObject();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroFormDict.setDirect(true);
        acroFormFields.add(signatureField);
        acroForm.setDefaultAppearance("/sylfaen 0 Tf 0 g");
        pdfStructure.setAcroFormFields(acroFormFields);
        pdfStructure.setAcroFormDictionary(acroFormDict);
        Log.i("PdfBox-Android", "AcroForm dictionary has been created");
    }

    @Override
    public void createSignatureRectangle(PDSignatureField signatureField,
                                         PDVisibleSignDesigner properties) throws IOException
    {

        PDRectangle rect = new PDRectangle();
        rect.setUpperRightX(properties.getxAxis() + properties.getWidth());
        rect.setUpperRightY(properties.getTemplateHeight() - properties.getyAxis());
        rect.setLowerLeftY(properties.getTemplateHeight() - properties.getyAxis() -
                           properties.getHeight());
        rect.setLowerLeftX(properties.getxAxis());
        signatureField.getWidgets().get(0).setRectangle(rect);
        pdfStructure.setSignatureRectangle(rect);
        Log.i("PdfBox-Android", "Signature rectangle has been created");
    }

    @Override
    public void createAffineTransform(byte[] params)
    {
        AffineTransform transform = new AffineTransform(params[0], params[1], params[2], params[3],
            params[4], params[5]);
        pdfStructure.setAffineTransform(transform);
        Log.i("PdfBox-Android", "Matrix has been added");
    }

    @Override
    public void createProcSetArray()
    {
        COSArray procSetArr = new COSArray();
        procSetArr.add(COSName.getPDFName("PDF"));
        procSetArr.add(COSName.getPDFName("Text"));
        procSetArr.add(COSName.getPDFName("ImageB"));
        procSetArr.add(COSName.getPDFName("ImageC"));
        procSetArr.add(COSName.getPDFName("ImageI"));
        pdfStructure.setProcSet(procSetArr);
        Log.i("PdfBox-Android", "ProcSet array has been created");
    }

    @Override
    public void createSignatureImage(PDDocument template, Bitmap image) throws IOException
    {
        pdfStructure.setImage(LosslessFactory.createFromImage(template, image));
        Log.i("PdfBox-Android", "Visible Signature Image has been created");
    }

    @Override
    public void createFormatterRectangle(byte[] params)
    {
        PDRectangle formatterRectangle = new PDRectangle();
        formatterRectangle.setUpperRightX(params[0]);
        formatterRectangle.setUpperRightY(params[1]);
        formatterRectangle.setLowerLeftX(params[2]);
        formatterRectangle.setLowerLeftY(params[3]);

        pdfStructure.setFormatterRectangle(formatterRectangle);
        Log.i("PdfBox-Android", "Formatter rectangle has been created");
    }

    @Override
    public void createHolderFormStream(PDDocument template)
    {
        PDStream holderForm = new PDStream(template);
        pdfStructure.setHolderFormStream(holderForm);
        Log.i("PdfBox-Android", "Holder form stream has been created");
    }

    @Override
    public void createHolderFormResources()
    {
        PDResources holderFormResources = new PDResources();
        pdfStructure.setHolderFormResources(holderFormResources);
        Log.i("PdfBox-Android", "Holder form resources have been created");

    }

    @Override
    public void createHolderForm(PDResources holderFormResources, PDStream holderFormStream,
                                 PDRectangle formrect)
    {
        PDFormXObject holderForm = new PDFormXObject(holderFormStream);
        holderForm.setResources(holderFormResources);
        holderForm.setBBox(formrect);
        holderForm.setFormType(1);
        pdfStructure.setHolderForm(holderForm);
        Log.i("PdfBox-Android", "Holder form has been created");

    }

    @Override
    public void createAppearanceDictionary(PDFormXObject holderForml,
                                           PDSignatureField signatureField) throws IOException
    {
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);

        PDAppearanceStream appearanceStream = new PDAppearanceStream(holderForml.getCOSObject());

        appearance.setNormalAppearance(appearanceStream);
        signatureField.getWidgets().get(0).setAppearance(appearance);

        pdfStructure.setAppearanceDictionary(appearance);
        Log.i("PdfBox-Android", "PDF appearance dictionary has been created");
    }

    @Override
    public void createInnerFormStream(PDDocument template)
    {
        PDStream innterFormStream = new PDStream(template);
        pdfStructure.setInnterFormStream(innterFormStream);
        Log.i("PdfBox-Android",
            "Stream of another form (inner form - it will be inside holder form) has been created");
    }

    @Override
    public void createInnerFormResource()
    {
        PDResources innerFormResources = new PDResources();
        pdfStructure.setInnerFormResources(innerFormResources);
        Log.i("PdfBox-Android",
            "Resources of another form (inner form - it will be inside holder form) has been created");
    }

    @Override
    public void createInnerForm(PDResources innerFormResources, PDStream innerFormStream,
                                PDRectangle formrect)
    {
        PDFormXObject innerForm = new PDFormXObject(innerFormStream);
        innerForm.setResources(innerFormResources);
        innerForm.setBBox(formrect);
        innerForm.setFormType(1);
        pdfStructure.setInnerForm(innerForm);
        Log.i("PdfBox-Android",
            "Another form (inner form - it will be inside holder form) has been created");
    }

    @Override
    public void insertInnerFormToHolderResources(PDFormXObject innerForm,
                                                PDResources holderFormResources)
    {
        COSName innerFormName = holderFormResources.add(innerForm, "FRM");
        pdfStructure.setInnerFormName(innerFormName);
        Log.i("PdfBox-Android", "Now inserted inner form inside holder form");
    }

    @Override
    public void createImageFormStream(PDDocument template)
    {
        PDStream imageFormStream = new PDStream(template);
        pdfStructure.setImageFormStream(imageFormStream);
        Log.i("PdfBox-Android", "Created image form stream");
    }

    @Override
    public void createImageFormResources()
    {
        PDResources imageFormResources = new PDResources();
        pdfStructure.setImageFormResources(imageFormResources);
        Log.i("PdfBox-Android", "Created image form Resources");
    }

    @Override
    public void createImageForm(PDResources imageFormResources, PDResources innerFormResource,
                                PDStream imageFormStream, PDRectangle formrect, AffineTransform at,
                                PDImageXObject img) throws IOException
    {
        PDFormXObject imageForm = new PDFormXObject(imageFormStream);
        imageForm.setBBox(formrect);
        imageForm.setMatrix(at);
        imageForm.setResources(imageFormResources);
        imageForm.setFormType(1);

        imageFormResources.getCOSObject().setDirect(true);

        COSName imageFormName = innerFormResource.add(imageForm, "n");
        COSName imageName = imageFormResources.add(img, "img");
        pdfStructure.setImageForm(imageForm);
        pdfStructure.setImageFormName(imageFormName);
        pdfStructure.setImageName(imageName);
        Log.i("PdfBox-Android", "Created image form");
    }

    @Override
    public void injectProcSetArray(PDFormXObject innerForm, PDPage page,
                                   PDResources innerFormResources,  PDResources imageFormResources,
                                   PDResources holderFormResources, COSArray procSet)
    {
        innerForm.getResources().getCOSObject().setItem(COSName.PROC_SET, procSet);
        page.getCOSObject().setItem(COSName.PROC_SET, procSet);
        innerFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        imageFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        holderFormResources.getCOSObject().setItem(COSName.PROC_SET, procSet);
        Log.i("PdfBox-Android", "Inserted ProcSet to PDF");
    }

    @Override
    public void injectAppearanceStreams(PDStream holderFormStream, PDStream innterFormStream,
                                        PDStream imageFormStream, COSName imageObjectName,
                                        COSName imageName, COSName innerFormName,
                                        PDVisibleSignDesigner properties) throws IOException
    {
        // 100 means that document width is 100% via the rectangle. if rectangle
        // is 500px, images 100% is 500px.
        // String imgFormComment = "q "+imageWidthSize+ " 0 0 50 0 0 cm /" +
        // imageName + " Do Q\n" + builder.toString();
        String imgFormComment = "q " + 100 + " 0 0 50 0 0 cm /" + imageName.getName() + " Do Q\n";
        String holderFormComment = "q 1 0 0 1 0 0 cm /" + innerFormName.getName() + " Do Q \n";
        String innerFormComment = "q 1 0 0 1 0 0 cm /" + imageObjectName.getName() + " Do Q\n";

        appendRawCommands(pdfStructure.getHolderFormStream().createOutputStream(),
                holderFormComment);
        appendRawCommands(pdfStructure.getInnerFormStream().createOutputStream(),
                innerFormComment);
        appendRawCommands(pdfStructure.getImageFormStream().createOutputStream(),
                imgFormComment);
        Log.i("PdfBox-Android", "Injected appearance stream to pdf");
    }

    public void appendRawCommands(OutputStream os, String commands) throws IOException
    {
        os.write(commands.getBytes("UTF-8"));
        os.close();
    }

    @Override
    public void createVisualSignature(PDDocument template)
    {
        pdfStructure.setVisualSignature(template.getDocument());
        Log.i("PdfBox-Android", "Visible signature has been created");
    }

    @Override
    public void createWidgetDictionary(PDSignatureField signatureField,
                                       PDResources holderFormResources) throws IOException
    {
        COSDictionary widgetDict = signatureField.getWidgets().get(0).getCOSObject();
        widgetDict.setNeedToBeUpdated(true);
        widgetDict.setItem(COSName.DR, holderFormResources.getCOSObject());

        pdfStructure.setWidgetDictionary(widgetDict);
        Log.i("PdfBox-Android", "WidgetDictionary has been created");
    }

    @Override
    public void closeTemplate(PDDocument template) throws IOException
    {
        template.close();
        pdfStructure.getTemplate().close();
    }
}
