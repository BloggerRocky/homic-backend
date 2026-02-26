package com.example.homic.utils.notUse;

public  class PictureUploader {
//    @Transactional
//    public String UploadImg(MultipartFile file, String Path, String id, String oldUrl)
//    {
//        String originName = file.getOriginalFilename();//获取文件名
//        String type = originName.substring(originName.lastIndexOf('.')+1);//获取文件后缀
//        if(!ArrayUtils.contains(PICTURE_TYPE_ARRAY,type))//文件类型校验
//            return null;
//        else
//        {
//            if(oldUrl!=null && !oldUrl.equals(""))//如果用户信息中的图片地址不为空，先删除旧图片
//            {
//                File oldFile = new File(oldUrl);
//                if (oldFile.exists())
//                    oldFile.delete();
//            }
//            //生成日期标注
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//            String format = sdf.format(new Date());
//            // 保存文件的文件夹
//            File folder = new File(savePath+"/"+subPath);
//            // 判断路径是否存在,不存在则自动创建
//            if(!folder.exists()){
//                System.out.println("正在创建文件夹");
//                folder.mkdirs();
//            }
//            String saveName = id+format+"."+type;
//            try {
//                String filePath = subPath+"/"+ saveName;
//                System.out.println(savePath+"/"+filePath);
//                file.transferTo(new File(savePath+"/"+filePath));
//
//                return new ImgResult(true,filePath);
//            } catch (IOException e){
//                return new ImgResult(false,null);
//            }
//
//        }
//
//    }

}
