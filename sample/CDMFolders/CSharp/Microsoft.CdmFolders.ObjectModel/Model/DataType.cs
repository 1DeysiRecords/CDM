﻿// <copyright file="DataType.cs" company="Microsoft">
// Copyright (c) Microsoft. All rights reserved.
// </copyright>

namespace Microsoft.CdmFolders.ObjectModel
{
    using Microsoft.CdmFolders.ObjectModel.SerializationHelpers;
    using Newtonsoft.Json;

    /// <summary>
    /// Provides enumerated values for a data type.
    /// </summary>
    [JsonConverter(typeof(StringEnumCamelCaseConverter))]
    public enum DataType
    {
        /// <summary>
        /// Unclassified
        /// </summary>
        Unclassified,

        /// <summary>
        /// String
        /// </summary>
        String,

        /// <summary>
        /// Int64
        /// </summary>
        Int64,

        /// <summary>
        /// Double
        /// </summary>
        Double,

        /// <summary>
        /// DateTime
        /// </summary>
        DateTime,

        /// <summary>
        /// DateTimeOffset
        /// </summary>
        DateTimeOffset,

        /// <summary>
        /// Decimal
        /// </summary>
        Decimal,

        /// <summary>
        /// Boolean
        /// </summary>
        Boolean,

        /// <summary>
        /// GUID
        /// </summary>
        Guid,

        /// <summary>
        /// Serialized json
        /// </summary>
        Json,
    }
}