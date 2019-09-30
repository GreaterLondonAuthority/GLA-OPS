<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lqb="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <xsl:template match="/">
        <html>
            <head>
                <style>
                    body {background-color: #ddd; font-family: arial;}
                </style>
            </head>
            <body>
                <h1>Change Log</h1>

                <xsl:if test="//lqb:createTable/lqb:column">
                    <h2>Tables Added</h2>
                    <table border="2">
                        <tr>
                            <th>Table</th>
                            <th>Column</th>
                            <th>Type</th>
                            <th>Remarks</th>
                        </tr>
                        <xsl:for-each select="//lqb:createTable/lqb:column">
                            <tr>
                                <td><xsl:value-of select="../@tableName"/></td>
                                <td><xsl:value-of select="@name"/></td>
                                <td><xsl:value-of select="@type"/></td>
                                <td><xsl:value-of select="@remarks"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

                <xsl:if test="//lqb:addColumn/lqb:column">
                    <h2>Columns Added</h2>
                    <table border="2">
                        <tr>
                            <th>Table</th>
                            <th>Column</th>
                            <th>Type</th>
                            <th>Remarks</th>
                        </tr>
                        <xsl:for-each select="//lqb:addColumn/lqb:column">
                            <tr>
                                <td><xsl:value-of select="../@tableName"/></td>
                                <td><xsl:value-of select="@name"/></td>
                                <td><xsl:value-of select="@type"/></td>
                                <td><xsl:value-of select="@remarks"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

                <xsl:if test="//lqb:dropTable">
                    <h2>Tables Deleted</h2>
                    <table border="2">
                        <tr>
                            <th>Table</th>
                        </tr>
                        <xsl:for-each select="//lqb:dropTable">
                            <tr>
                                <td><xsl:value-of select="@tableName"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

                <xsl:if test="//lqb:dropColumn">
                    <h2>Columns Deleted</h2>
                    <table border="2">
                        <tr>
                            <th>Table</th>
                            <th>Column</th>
                        </tr>
                        <xsl:for-each select="//lqb:dropColumn">
                            <tr>
                                <td><xsl:value-of select="@tableName"/></td>
                                <td><xsl:value-of select="@columnName"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

                <xsl:if test="//lqb:renameTable">
                    <h2>Tables Renamed</h2>
                    <table border="2">
                        <tr>
                            <th>Old Table Name</th>
                            <th>New Table Name</th>
                        </tr>
                        <xsl:for-each select="//lqb:renameTable">
                            <tr>
                                <td><xsl:value-of select="@oldTableName"/></td>
                                <td><xsl:value-of select="@newTableName"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

                <xsl:if test="//lqb:renameColumn">
                    <h2>Columns Renamed</h2>
                    <table border="2">
                        <tr>
                            <th>Table</th>
                            <th>Old Column Name</th>
                            <th>New Column Name</th>
                            <th>Remarks</th>
                        </tr>
                        <xsl:for-each select="//lqb:renameColumn">
                            <tr>
                                <td><xsl:value-of select="@tableName"/></td>
                                <td><xsl:value-of select="@oldColumnName"/></td>
                                <td><xsl:value-of select="@newColumnName"/></td>
                                <td><xsl:value-of select="@remarks"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </xsl:if>

            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>