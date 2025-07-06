package com.pdfmanager.utils;

import com.pdfmanager.files.Book;
import com.pdfmanager.files.Collection;
import com.pdfmanager.files.DocumentType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe utilitária para gerar arquivos de referência BibTeX a partir de uma coleção de livros.
 */
public class BibTexGenerator {

    /**
     * Gera e salva um arquivo .bib com as referências dos livros em uma coleção.
     * @param collection A coleção da qual extrair as referências.
     * @param books A lista de objetos Book completos que correspondem aos títulos na coleção.
     * @param outputPath O caminho completo onde o arquivo .bib deve ser salvo.
     * @throws IOException Se ocorrer um erro durante a escrita do arquivo.
     */
    public static void generate(Collection collection, List<Book> books, Path outputPath) throws IOException {
        if (collection.getType() != DocumentType.BOOK) {
            System.err.println("Geração de BibTeX é suportada apenas para coleções do tipo 'BOOK'.");
            return;
        }

        StringBuilder bibtexContent = new StringBuilder();
        bibtexContent.append("% Arquivo BibTeX gerado pelo PDF Manager\n\n");

        for (Book book : books) {
            // Gera uma chave de citação simples. Ex: "Machado2023"
            String authorLastName = book.getAuthors().get(0).split(" ")[0].replaceAll("[^a-zA-Z]", "");
            String citationKey = authorLastName + book.getPublishYear();

            bibtexContent.append("@book{").append(citationKey).append(",\n");

            // Junta a lista de autores com " and "
            String authorsString = String.join(" and ", book.getAuthors());
            bibtexContent.append(String.format("  author    = {%s},\n", authorsString));
            bibtexContent.append(String.format("  title     = {%s},\n", book.getTitle()));

            // Adiciona campos opcionais se eles não forem nulos ou vazios
            if (book.getSubTitle() != null && !book.getSubTitle().isEmpty()) {
                bibtexContent.append(String.format("  subtitle  = {%s},\n", book.getSubTitle()));
            }
            if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                bibtexContent.append(String.format("  publisher = {%s},\n", book.getPublisher()));
            }

            bibtexContent.append(String.format("  year      = {%d}\n", book.getPublishYear()));
            bibtexContent.append("}\n\n");
        }

        // Usa try-with-resources para garantir que o FileWriter seja fechado automaticamente
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            writer.write(bibtexContent.toString());
        }
    }
}