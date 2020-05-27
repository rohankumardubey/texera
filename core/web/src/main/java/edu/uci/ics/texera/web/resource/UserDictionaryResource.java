package edu.uci.ics.texera.web.resource;

import static org.jooq.impl.DSL.defaultValue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.types.UInteger;

import static edu.uci.ics.texera.dataflow.jooq.generated.Tables.*;
import static org.jooq.impl.DSL.*;

import edu.uci.ics.texera.web.TexeraWebException;
import edu.uci.ics.texera.web.response.GenericWebResponse;
import edu.uci.ics.texera.dataflow.sqlServerInfo.UserSqlServer;


@Path("/user/dictionary")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserDictionaryResource {
    
    /**
     * Corresponds to `src/app/dashboard/type/user-dictionary.ts`
     */
    public static class UserDictionary {
        public UInteger id; // the ID in MySQL database is unsigned int
        public String name;
        public List<String> items;
        public String description;
        
        public UserDictionary() {} // default constructor reserved for json

        public UserDictionary(UInteger id, String name, List<String> items, String description) {
            this.id = id;
            this.name = name;
            this.items = items;
            this.description = description;
        }
    }
    
    /**
     * Corresponds to `src/app/dashboard/type/user-dictionary.ts`
     */
    public static class UserManualDictionary {
        public String name;
        public String content;
        public String separator;
        public String description;

        public UserManualDictionary() { } // default constructor reserved for json
        
        public boolean isValid() {
            return name != null && name.length() != 0 &&
                    content != null && content.length() != 0 &&
                    separator != null && description != null;
        }
    }
    
    @PUT
    @Path("/upload-manual-dict/{userID}")
    public GenericWebResponse putManualDictionary(
            @PathParam("userID") String userID,
            UserManualDictionary userManualDictionary
    ) {
        if (userManualDictionary == null || !userManualDictionary.isValid()) {
            throw new TexeraWebException("Error occurred in user manual dictionary");
        }
        UInteger userIdUInteger = parseStringToUInteger(userID);
        
        List<String> itemArray = convertStringToList(
                userManualDictionary.content, 
                userManualDictionary.separator
                );
        byte[] contentByteArray = convertListToByteArray(itemArray);
        
        int count = insertDictionaryToDataBase(
                userManualDictionary.name, 
                contentByteArray,
                userManualDictionary.description,
                userIdUInteger);
        
        throwErrorWhenNotOne("Error occurred while inserting dictionary to database", count);
        
        return new GenericWebResponse(0, "success");
    }
    
    @POST
    @Path("/upload/{userID}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericWebResponse putDictionary(
            @PathParam("userID") String userID,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("description") String description
    ) {
        UInteger userIdUInteger = parseStringToUInteger(userID);
        String fileName = fileDetail.getFileName();
        String separator = ",";
        
        String content = readFileContent(uploadedInputStream);
        List<String> itemList = convertStringToList(content, separator);
        byte[] contentByteArray = convertListToByteArray(itemList);
        
        int count = insertDictionaryToDataBase(
                fileName,
                contentByteArray,
                description,
                userIdUInteger);
        
        throwErrorWhenNotOne("Error occurred while inserting dictionary to database", count);
        
        return new GenericWebResponse(0, "success");
    }
    
    @GET
    @Path("/list/{userID}")
    public List<UserDictionary> getDictionary(
            @PathParam("userID") String userID
    ) {
        UInteger userIdUInteger = parseStringToUInteger(userID);
        
        Result<Record4<UInteger, String, byte[], String>> result = getUserDictionaryRecord(userIdUInteger);
        
        if (result == null) return new ArrayList<>();
        
        List<UserDictionary> dictionaryList = result.stream()
                .map(
                    record -> new UserDictionary(
                            record.get(USERDICT.DICTID),
                            record.get(USERDICT.NAME),
                            convertContentToList(record.get(USERDICT.CONTENT)),
                            record.get(USERDICT.DESCRIPTION)
                            )
                        ).collect(Collectors.toList());
        
        return dictionaryList;
    }
    
    @DELETE
    @Path("/delete/{dictID}")
    public GenericWebResponse deleteDictionary(
            @PathParam("dictID") String dictID
    ) {
        UInteger dictIdUInteger = parseStringToUInteger(dictID);
        
        int count = deleteInDatabase(dictIdUInteger);
        throwErrorWhenNotOne("delete dictionary " + dictIdUInteger + " failed in database", count);
        
        return new GenericWebResponse(0, "success");
    }
    
    @POST
    @Path("/update")
    public GenericWebResponse updateDictionary(
            UserDictionary userDictionary
    ) {
        byte[] contentByteArray = convertListToByteArray(userDictionary.items);
        
        int count = updateInDatabase(
                userDictionary.id,
                userDictionary.name, 
                contentByteArray,
                userDictionary.description
                );
        
        throwErrorWhenNotOne("Error occurred while inserting dictionary to database", count);
        
        return new GenericWebResponse(0, "success");
    }
    
    private int updateInDatabase(UInteger dictID, String name, byte[] content, String description) {
        // Connection is AutoCloseable so it will automatically close when it finishes.
        try (Connection conn = UserSqlServer.getConnection()) {
            DSLContext create = UserSqlServer.createDSLContext(conn);
            
            int count = create
                    .update(USERDICT)
                    .set(USERDICT.NAME, name)
                    .set(USERDICT.CONTENT, content)
                    .set(USERDICT.DESCRIPTION, description)
                    .where(USERDICT.DICTID.eq((dictID)))
                    .execute();
            
            return count;
            
        } catch (Exception e) {
            throw new TexeraWebException(e);
        }
    }
    
    private int deleteInDatabase(UInteger dictID) {
        // Connection is AutoCloseable so it will automatically close when it finishes.
        try (Connection conn = UserSqlServer.getConnection()) {
            DSLContext create = UserSqlServer.createDSLContext(conn);
            
            int count = create
                    .delete(USERDICT)
                    .where(USERDICT.DICTID.eq(dictID))
                    .execute();
            
            return count;
            
        } catch (Exception e) {
            throw new TexeraWebException(e);
        }
    }
    
    private Result<Record4<UInteger, String, byte[], String>> getUserDictionaryRecord(UInteger userID) {
        // Connection is AutoCloseable so it will automatically close when it finishes.
        try (Connection conn = UserSqlServer.getConnection()) {
            DSLContext create = UserSqlServer.createDSLContext(conn);
            
            Result<Record4<UInteger, String, byte[], String>> result = create
                    .select(USERDICT.DICTID, USERDICT.NAME, USERDICT.CONTENT, USERDICT.DESCRIPTION)
                    .from(USERDICT)
                    .where(USERDICT.USERID.equal(userID))
                    .fetch();
            
            return result;
            
        } catch (Exception e) {
            throw new TexeraWebException(e);
        }
    }
    
    private int insertDictionaryToDataBase(String name, byte[] content, String description, UInteger userID) {
        // Connection is AutoCloseable so it will automatically close when it finishes.
        try (Connection conn = UserSqlServer.getConnection()) {
            DSLContext create = UserSqlServer.createDSLContext(conn);
            
            int count = create.insertInto(USERDICT)
                    .set(USERDICT.USERID,userID)
                    .set(USERDICT.DICTID, defaultValue(USERDICT.DICTID))
                    .set(USERDICT.NAME, name)
                    .set(USERDICT.CONTENT, content)
                    .set(USERDICT.DESCRIPTION, description)
                    .execute();
            
            return count;
            
        } catch (Exception e) {
            throw new TexeraWebException(e);
        }
    }
    
    /**
     * write the whole list into the byte array.
     * @param list
     * @return
     */
    private byte[] convertListToByteArray(List<String> list) {
            try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream objOstream = new ObjectOutputStream(baos))
            {
                objOstream.writeObject(list);
                return baos.toByteArray();
            }
            catch (Exception e){
                throw new TexeraWebException("Error when converting list of dictionary items into byte array");
            }
    }
    
    /**
     * convert the input byte array to the list of string
     * the result list is converted back from byte array so it is unchecked.
     * @param content
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<String> convertContentToList(byte[] content) {
        try(
                ByteArrayInputStream bais = new ByteArrayInputStream(content);
                ObjectInputStream ois = new ObjectInputStream(bais)){
            return (ArrayList<String>) ois.readObject();
        } catch (Exception e) {
            throw new TexeraWebException(e);
        }
    }
    
    private UInteger parseStringToUInteger(String userID) throws TexeraWebException {
        try {
            return UInteger.valueOf(userID);
        } catch (NumberFormatException e) {
            throw new TexeraWebException("Incorrect String to long");
        }
    }
    
    private String readFileContent(InputStream fileStream) {
        StringBuilder fileContents = new StringBuilder();
        String line;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream))) {
            while ((line = br.readLine()) != null) {
                fileContents.append(line);
            }
        } catch (IOException e) {
            throw new TexeraWebException(e);
        }
        return fileContents.toString();
    }
    
    private List<String> convertStringToList(String content, String separator) {
        return Stream.of(
                    content.trim().split(separator)
                )
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Most the sql operation should only be executed once. eg. insertion, deletion.
     * this method will raise TexeraWebException when the input number is not one
     * @param errorMessage
     * @param count
     * @throws TexeraWebException
     */
    private void throwErrorWhenNotOne(String errorMessage, int count) throws TexeraWebException {
        if (count != 1) {
            throw new TexeraWebException(errorMessage);
        }
    }
}
