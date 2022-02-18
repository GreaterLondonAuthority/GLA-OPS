/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class FileRepositoryHelper extends JdbcDaoSupport {

    @Autowired
    public void setDs(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public void getBinaryFile(Integer id, final OutputStream out) {

        getJdbcTemplate().query("select file_content from file where id = " + id,
                new AbstractLobStreamingResultSetExtractor() {
                    protected void streamData(ResultSet rs) throws SQLException, IOException {
                        FileCopyUtils.copy(new DefaultLobHandler().getBlobAsBinaryStream(rs, 1), out);
                    }
                });
    }

    public void getBinaryFileWithoutClosingOutputStream(Integer id, final OutputStream out) {

        getJdbcTemplate().query("select file_content from file where id = " + id,
                new AbstractLobStreamingResultSetExtractor() {
                    protected void streamData(ResultSet rs) throws SQLException, IOException {
                        InputStream blobAsBinaryStream = new DefaultLobHandler().getBlobAsBinaryStream(rs, 1);
                        int copy = StreamUtils.copy(blobAsBinaryStream, out);
                        blobAsBinaryStream.close();
                    }
                });
    }

    public void saveBinaryFile(final Integer fileId, final InputStream in) throws IOException {
        final int fileSize = in.available();
        getJdbcTemplate().execute("update file set file_content = ? where id = ?",
                new AbstractLobCreatingPreparedStatementCallback(new DefaultLobHandler()) {
                    protected void setValues(PreparedStatement ps, LobCreator lobCreator)
                            throws SQLException, DataAccessException {
                        ps.setInt(2, fileId);
                        lobCreator.setBlobAsBinaryStream(ps, 1, in, fileSize);
                    }
                });
        in.close();
    }

}
