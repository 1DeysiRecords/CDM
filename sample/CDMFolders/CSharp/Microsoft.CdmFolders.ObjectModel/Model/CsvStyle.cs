﻿// <copyright file="CsvStyle.cs" company="Microsoft">
// Copyright (c) Microsoft. All rights reserved.
// </copyright>

namespace Microsoft.CdmFolders.ObjectModel
{
    using System.Runtime.Serialization;
    using Newtonsoft.Json;
    using Newtonsoft.Json.Converters;

    /// <summary>
    /// CSV style settings.
    /// </summary>
    [JsonConverter(typeof(StringEnumConverter))]
    public enum CsvStyle
    {
        /// <summary>
        /// CSV quote style
        /// </summary>
        [EnumMember(Value = "CsvStyle.QuoteAlways")]
        QuoteAlways,

        /// <summary>
        /// No quotes
        /// </summary>
        [EnumMember(Value = "CsvStyle.QuoteAfterDelimiter")]
        QuoteAfterDelimiter,
    }
}
