package slanglsp;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes Slang identifiers for indexing and Find Usages. LSP4IJ remains
 * responsible for semantic references; this class never performs text renames.
 */
public final class SlangFindUsagesProvider implements FindUsagesProvider {
    private static final TokenSet IDENTIFIERS = TokenSet.create(
        SlangLexer.VARIABLE, SlangLexer.FUNCTION, SlangLexer.TYPE);

    @Override
    public WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
            new SlangLexer(), IDENTIFIERS,
            TokenSet.create(SlangLexer.COMMENT), TokenSet.create(SlangLexer.STRING));
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        if (element.getContainingFile() == null
            || !element.getContainingFile().getLanguage().isKindOf(SlangLanguage.INSTANCE)
            || element.getNode() == null) return false;
        IElementType type = element.getNode().getElementType();
        return IDENTIFIERS.contains(type);
    }

    @Override
    public String getHelpId(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public @NotNull String getType(@NotNull PsiElement element) {
        return "Slang symbol";
    }

    @Override
    public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        return element.getText();
    }

    @Override
    public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return element.getText();
    }
}
