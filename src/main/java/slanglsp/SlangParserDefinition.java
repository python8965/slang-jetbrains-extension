package slanglsp;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Maintains a token-level PSI without attempting to reimplement Slang semantics.
 * Cross-file resolution belongs to slangd; syntax colorization keeps using SlangLexer.
 */
public final class SlangParserDefinition implements ParserDefinition {
    private static final IFileElementType FILE_TYPE = new IFileElementType(SlangLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new SlangLexer();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return (fileType, tokens) -> {
            var file = tokens.mark();
            while (!tokens.eof()) tokens.advanceLexer();
            file.done(fileType);
            return tokens.getTreeBuilt();
        };
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE_TYPE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.create(SlangLexer.COMMENT);
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return TokenSet.create(SlangLexer.STRING);
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        return new ASTWrapperPsiElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider provider) {
        return new PsiFileBase(provider, SlangLanguage.INSTANCE) {
            @Override
            public @NotNull FileType getFileType() {
                return SlangFileType.INSTANCE;
            }
        };
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
