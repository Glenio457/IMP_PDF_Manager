package com.pdfmanager.utils;

import com.pdfmanager.files.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Classe utilitária para empacotar os arquivos de uma coleção em um arquivo .zip.
 */
public class CollectionPackager {

    /**
     * Cria um arquivo .zip contendo os arquivos físicos dos documentos de uma coleção.
     * @param documents A lista de objetos Document completos a serem compactados.
     * @param zipFilePath O caminho completo onde o arquivo .zip deve ser salvo (incluindo o nome do arquivo, ex: "C:/temp/colecao.zip").
     * @throws IOException Se ocorrer um erro durante a leitura dos arquivos ou a escrita do .zip.
     */
    public static void pack(List<Document> documents, Path zipFilePath) throws IOException {
        // Usa try-with-resources para garantir que os streams sejam fechados automaticamente.
        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            System.out.println("Criando arquivo zip em: " + zipFilePath);

            for (Document doc : documents) {
                // ASSUNÇÃO IMPORTANTE: O nome do arquivo físico não está salvo no modelo de dados.
                // Estamos assumindo que o nome do arquivo é igual ao seu 'título' + a extensão ".pdf".
                // Para maior robustez, o ideal seria adicionar um campo 'fileName' na classe Document.
                String fileName = doc.getTitle();
                File fileToZip = new File(doc.getPath(), fileName);

                if (!fileToZip.exists()) {
                    System.err.println("AVISO: Arquivo não encontrado, pulando: " + fileToZip.getAbsolutePath());
                    continue; // Pula para o próximo arquivo
                }

                // Usa um try-with-resources aninhado para o stream de leitura do arquivo
                try (FileInputStream fis = new FileInputStream(fileToZip)) {
                    ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                    System.out.println("Adicionado ao zip: " + fileToZip.getName());
                }
                zos.closeEntry();
            }
            System.out.println("Criação do arquivo zip concluída com sucesso.");
        }
    }
}