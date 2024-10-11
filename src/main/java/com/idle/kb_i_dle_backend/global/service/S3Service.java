package com.idle.kb_i_dle_backend.global.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.idle.kb_i_dle_backend.config.exception.CustomException;
import com.idle.kb_i_dle_backend.global.codes.ErrorCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3Client amazonS3Client;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * multipart 파일 받아서 file로 convert해서 upload함수 호출
     *
     * @param file
     * @param dirName
     * @return
     * @throws IOException
     */
    public String upload(MultipartFile file, String dirName) throws IOException {
        File uploadFile = convert(file).orElseThrow(() -> new CustomException(ErrorCode.INVALID_FILE, "파일 변환중 오류"));
        return upload(uploadFile, dirName);
    }


    /**
     * file s3에 업로드 후 변환과정중 생성된 파일 삭제
     *
     * @param uploadFile
     * @param dirName
     * @return
     */
    private String upload(File uploadFile, String dirName) {

        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);

        removeNewFile(uploadFile); // convert() 과정에서 로컬에 생성된 파일 삭제

        return uploadImageUrl;
    }

    /**
     * 파일 삭제
     *
     * @param targetFile
     */
    private void removeNewFile(File targetFile) {

        String name = targetFile.getName();

        // convert() 과정에서 로컬에 생성된 파일을 삭제
        if (targetFile.delete()) {
            log.info(name + "파일 삭제 완료");
        } else {
            log.info(name + "파일 삭제 실패");
        }
    }

    /**
     * s3업로드
     *
     * @param uploadFile
     * @param fileName
     * @return
     */
    private String putS3(File uploadFile, String fileName) {

        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile) // PublicRead 권한으로 upload
        );

        return amazonS3Client.getUrl(bucket, fileName).toString(); // File의 URL return
    }

    /**
     * multipartfile file로 업로드
     *
     * @param multipartFile
     * @return
     * @throws IOException
     */
    private Optional<File> convert(MultipartFile multipartFile) throws IOException {

        // 기존 파일 이름으로 새로운 File 객체 생성
        // 해당 객체는 프로그램이 실행되는 로컬 디렉토리(루트 디렉토리)에 위치하게 됨
        File convertFile = new File(multipartFile.getOriginalFilename());

        if (convertFile.createNewFile()) { // 해당 경로에 파일이 없을 경우, 새 파일 생성

            try (FileOutputStream fos = new FileOutputStream(convertFile)) {

                // multipartFile의 내용을 byte로 가져와서 write
                fos.write(multipartFile.getBytes());
            }
            return Optional.of(convertFile);
        }

        // 새파일이 성공적으로 생성되지 않았다면, 비어있는 Optional 객체를 반환
        return Optional.empty();
    }
}
