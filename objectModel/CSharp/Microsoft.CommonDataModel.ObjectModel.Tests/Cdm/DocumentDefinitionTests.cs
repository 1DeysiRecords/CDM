﻿// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License. See License.txt in the project root for license information.

namespace Microsoft.CommonDataModel.ObjectModel.Tests.Cdm
{
    using Microsoft.CommonDataModel.ObjectModel.Cdm;
    using Microsoft.CommonDataModel.ObjectModel.Utilities;
    using Microsoft.VisualStudio.TestTools.UnitTesting;
    using System.Threading.Tasks;

    /// <summary>
    /// Tests for the CdmDocumentDefinition class.
    /// </summary>
    [TestClass]
    public class DocumentDefinitionTests
    {
        /// <summary>
        /// Test when A -> M/B -> C -> B.
        /// In this case, although A imports B with a moniker, B should be in the priorityImports because it is imported by C.
        /// </summary>
        [TestMethod]
        public async Task TestCircularImportWithMoniker()
        {
            var corpus = TestHelper.GetLocalCorpus("", "");
            var folder = corpus.Storage.FetchRootFolder("local");

            var docA = new CdmDocumentDefinition(corpus.Ctx, "A.cdm.json");
            folder.Documents.Add(docA);
            docA.Imports.Add("B.cdm.json", "moniker");

            var docB = new CdmDocumentDefinition(corpus.Ctx, "B.cdm.json");
            folder.Documents.Add(docB);
            docB.Imports.Add("C.cdm.json");

            var docC = new CdmDocumentDefinition(corpus.Ctx, "C.cdm.json");
            folder.Documents.Add(docC);
            docC.Imports.Add("B.cdm.json");

            // forces docB to be indexed first.
            await docB.IndexIfNeeded(new ResolveOptions());
            await docA.IndexIfNeeded(new ResolveOptions());

            // should contain A, B and C.
            Assert.AreEqual(3, docA.ImportPriorities.ImportPriority.Count);

            Assert.IsFalse(docA.ImportPriorities.hasCircularImport);

            // docB and docC should have the hasCircularImport set to true.
            Assert.IsTrue(docB.ImportPriorities.hasCircularImport);
            Assert.IsTrue(docC.ImportPriorities.hasCircularImport);
        }

        /// <summary>
        /// Test when A -> B -> C/M -> D -> C.
        /// In this case, although B imports C with a moniker, C should be in the A's priorityImports because it is imported by D.
        /// </summary>
        [TestMethod]
        public async Task TestDeeperCircularImportWithMoniker()
        {
            var corpus = TestHelper.GetLocalCorpus("", "");
            var folder = corpus.Storage.FetchRootFolder("local");

            var docA = new CdmDocumentDefinition(corpus.Ctx, "A.cdm.json");
            folder.Documents.Add(docA);
            docA.Imports.Add("B.cdm.json");

            var docB = new CdmDocumentDefinition(corpus.Ctx, "B.cdm.json");
            folder.Documents.Add(docB);
            docB.Imports.Add("C.cdm.json", "moniker");

            var docC = new CdmDocumentDefinition(corpus.Ctx, "C.cdm.json");
            folder.Documents.Add(docC);
            docC.Imports.Add("D.cdm.json");

            var docD = new CdmDocumentDefinition(corpus.Ctx, "D.cdm.json");
            folder.Documents.Add(docD);
            docD.Imports.Add("C.cdm.json");

            // indexIfNeeded will internally call prioritizeImports on every document.
            await docA.IndexIfNeeded(new ResolveOptions());

            Assert.AreEqual(4, docA.ImportPriorities.ImportPriority.Count);

            // reset the importsPriorities.
            MarkDocumentsToIndex(folder.Documents);

            // force docC to be indexed first, so the priorityList will be read from the cache this time.
            await docC.IndexIfNeeded(new ResolveOptions());
            await docA.IndexIfNeeded(new ResolveOptions());

            Assert.AreEqual(4, docA.ImportPriorities.ImportPriority.Count);

            // indexes the rest of the documents.
            await docB.IndexIfNeeded(new ResolveOptions());
            await docD.IndexIfNeeded(new ResolveOptions());

            Assert.IsFalse(docA.ImportPriorities.hasCircularImport);
            Assert.IsFalse(docB.ImportPriorities.hasCircularImport);
            Assert.IsTrue(docC.ImportPriorities.hasCircularImport);
            Assert.IsTrue(docD.ImportPriorities.hasCircularImport);
        }

        /// <summary>
        /// Test if monikered imports are not being added to the priorityList.
        /// A -> B/M -> C
        /// </summary>
        [TestMethod]
        public async Task TestMonikeredImportIsNotAdded()
        {
            var corpus = TestHelper.GetLocalCorpus("", "");
            var folder = corpus.Storage.FetchRootFolder("local");

            var docA = new CdmDocumentDefinition(corpus.Ctx, "A.cdm.json");
            folder.Documents.Add(docA);
            docA.Imports.Add("B.cdm.json", "moniker");

            var docB = new CdmDocumentDefinition(corpus.Ctx, "B.cdm.json");
            folder.Documents.Add(docB);
            docB.Imports.Add("C.cdm.json");
            
            var docC = new CdmDocumentDefinition(corpus.Ctx, "C.cdm.json");
            folder.Documents.Add(docC);

            // forces docB to be indexed first, so the priorityList will be read from the cache this time.
            await docB.IndexIfNeeded(new ResolveOptions(docB));
            await docA.IndexIfNeeded(new ResolveOptions(docA));

            // should only contain docA and docC, docB should be excluded.
            Assert.AreEqual(2, docA.ImportPriorities.ImportPriority.Count);

            Assert.IsFalse(docA.ImportPriorities.hasCircularImport);
            Assert.IsFalse(docB.ImportPriorities.hasCircularImport);
            Assert.IsFalse(docC.ImportPriorities.hasCircularImport);
        }

        /// <summary>
        /// Sets the document's isDirty flag to true and reset the importPriority.
        /// </summary>
        /// <param name="documents"></param>
        private void MarkDocumentsToIndex(CdmDocumentCollection documents)
        {
            foreach (var document in documents)
            {
                document.NeedsIndexing = true;
                document.ImportPriorities = null;
            }
        }
    }
}
