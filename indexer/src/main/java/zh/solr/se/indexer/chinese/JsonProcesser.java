package zh.solr.se.indexer.chinese;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.map.ObjectMapper;

import zh.solr.se.indexer.db.entity.ChineseEntity;

public class JsonProcesser {

        private ObjectMapper mapper;
        private static SimpleDateFormat dateFormator = new SimpleDateFormat(
                        "yyyyMMdd'T'HHmmss");

        public JsonProcesser(ObjectMapper mapper) {
                this.mapper = mapper;
        }

        public ChineseEntity parseDataModel(String jsonString)
                        throws Exception {
        	ChineseEntity dataModel = null;
                try {
                        dataModel = this.mapper.readValue(jsonString, ChineseEntity.class);
                } catch (IOException e) {
                        throw new Exception("can't map json to java Obejct, check input json String : " + jsonString, e);
                }
                return dataModel;
        }
        

        public String generateJsonFromEntity(Object object)
                        throws Exception {
                String jsonString = "";
                try {
                        jsonString = this.mapper.writeValueAsString(object);
                } catch (Exception e) {
                        throw new Exception("can't map java object to json string , id:" + ((ChineseEntity) object).getId() , e);
                }
                return jsonString;
        }

        public String dateFormat(Date date) {
                return JsonProcesser.dateFormator.format(date);
        }

}
