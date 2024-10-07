package io.innospots.workflow.core.response;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/4
 */
public enum CardViewType {
    table,
    chart,
    text,
    markdown,
    image,
    video,
    file,
    embed,
    normal;

    public static class CardMimeType{
         public static CardViewType getType(String mimeType){
             CardViewType cardViewType = null;
             if(mimeType == null){
             }else if(mimeType.contains("image")){
                 cardViewType = image;
             } else if (mimeType.contains("video")) {
                 cardViewType = video;
             } else if (mimeType.contains("application") || mimeType.contains("text")) {
                 cardViewType = file;
             }

             return cardViewType;
        }
    }
}
