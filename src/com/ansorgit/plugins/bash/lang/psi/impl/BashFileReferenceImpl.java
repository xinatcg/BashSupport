/*
 * Copyright 2013 Joachim Ansorg, mail@ansorg-it.com
 * File: BashFileReferenceImpl.java, Class: BashFileReferenceImpl
 * Last modified: 2013-05-02
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ansorgit.plugins.bash.lang.psi.impl;

import com.ansorgit.plugins.bash.lang.psi.BashVisitor;
import com.ansorgit.plugins.bash.lang.psi.api.BashCharSequence;
import com.ansorgit.plugins.bash.lang.psi.api.BashFileReference;
import com.ansorgit.plugins.bash.lang.psi.util.BashPsiFileUtils;
import com.ansorgit.plugins.bash.lang.psi.util.BashPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiFileReference;
import com.intellij.refactoring.rename.BindablePsiReference;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class BashFileReferenceImpl extends BashBaseElement implements BashFileReference {
    private FileReferenceSet fileReferenceSet;

    public BashFileReferenceImpl(final ASTNode astNode) {
        super(astNode, "Bash File reference");
    }

    @Nullable
    @Override
    public PsiFile findReferencedFile() {
        PsiReference reference = getReference();
        if (reference == null) {
            return null;
        }

        return (PsiFile) reference.resolve();
    }

    @NotNull
    public String getFilename() {
        PsiElement firstParam = getFirstChild();
        if (firstParam instanceof BashCharSequence) {
            return ((BashCharSequence) firstParam).getUnwrappedCharSequence();
        }

        return getText();
    }

    public boolean isStatic() {
        PsiElement firstChild = getFirstChild();
        return firstChild instanceof BashCharSequence && ((BashCharSequence) firstChild).isStatic();
    }

    @Override
    public PsiReference getReference() {
        return new CachingFileReference(this);
        //fixme
        //throw new IllegalStateException("not implemeneted");
        //return referenceSet().getLastReference();
    }

    @Override
    public boolean canNavigate() {
        PsiReference reference = getReference();

        return reference != null && reference.resolve() != null;
    }

    private FileReferenceSet referenceSet() {
        if (fileReferenceSet == null) {
            synchronized (this) {
                fileReferenceSet = new FileReferenceSet(this);
                fileReferenceSet.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, new Function<PsiFile, Collection<PsiFileSystemItem>>() {
                    @Override
                    public Collection<PsiFileSystemItem> fun(PsiFile psiFile) {
                        return Collections.<PsiFileSystemItem>singletonList(psiFile.getContainingDirectory());
                    }
                });
            }
        }

        return fileReferenceSet;
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof BashVisitor) {
            ((BashVisitor) visitor).visitFileReference(this);
        } else {
            visitor.visitElement(this);
        }
    }

    private static class CachingFileReference extends PsiReferenceBase<BashFileReferenceImpl> implements PsiReference, BindablePsiReference, PsiFileReference {
        public CachingFileReference(BashFileReferenceImpl myElement) {
            super(myElement, false);
        }

        @Override
        public BashFileReferenceImpl getElement() {
            return myElement;
        }

        public TextRange getRangeInElement() {
            ElementManipulator<BashFileReferenceImpl> manipulator = ElementManipulators.getManipulator(myElement);
            if (manipulator == null) {
                throw new IncorrectOperationException("no implementation found to rename " + myElement);
            }

            return manipulator.getRangeInElement(myElement);
        }

        @NotNull
        public String getCanonicalText() {
            //fixme
            return this.myElement.getText();
        }

        public PsiElement handleElementRename(String newName) throws IncorrectOperationException {
            ElementManipulator<BashFileReferenceImpl> manipulator = ElementManipulators.getManipulator(myElement);
            if (manipulator == null) {
                throw new IncorrectOperationException("no implementation found to rename " + myElement);
            }

            return manipulator.handleContentChange(myElement, newName);
        }

        public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
            return handleElementRename(((PsiFile) element).getName());
        }

        public boolean isReferenceTo(PsiElement element) {
            return element == resolve();
        }

        @NotNull
        public Object[] getVariants() {
            return PsiElement.EMPTY_ARRAY;
        }

        @Override
        public PsiElement resolve() {
            return resolveInner();
        }

        @Nullable
        public PsiElement resolveInner() {
            PsiFile containingFile = BashPsiUtils.findFileContext(getElement());
            return BashPsiFileUtils.findRelativeFile(containingFile, myElement.getFilename());
        }

        @NotNull
        @Override
        public ResolveResult[] multiResolve(boolean incompleteCode) {
            PsiElement element = resolve();

            return element != null ? new ResolveResult[]{new PsiElementResolveResult(element)} : ResolveResult.EMPTY_ARRAY;
        }
    }
}
