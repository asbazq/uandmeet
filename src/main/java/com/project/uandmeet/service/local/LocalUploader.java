package com.project.uandmeet.service.local;

import com.project.uandmeet.dto.ImageDto;
import com.project.uandmeet.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/*
 * 프로젝트를 aws에서 계속 실행하기에는 비용문제가 있어 로컬에서 실행
 * 이 때 s3 또한 비용문제가 있어 로컬업로더로 변경
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class LocalUploader {
    private final ImageRepository imageRepository;

    @Value("${local.upload.dir}")
    private String localUploadDir; // 로컬 업로드 디렉토리 경로

    public ImageDto upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        return upload(uploadFile, dirName);
    }

    // 로컬에서 파일 삭제
    public void deleteUserImage(Long imageId) {
        String fileName = imageRepository.findById(imageId).orElseThrow(IllegalArgumentException::new).getFilename();
        Path filePath = Paths.get(localUploadDir, fileName);
        try {
            Files.deleteIfExists(filePath);
            log.info("File delete success: " + filePath);
        } catch (IOException e) {
            log.error("File delete fail: " + filePath, e);
        }
    }

    // 로컬 파일 업로드
    private ImageDto upload(File uploadFile, String dirName) throws IOException {
        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
        Path filePath = Paths.get(localUploadDir, fileName);
        Files.createDirectories(filePath.getParent()); // 디렉토리 생성
        Files.copy(uploadFile.toPath(), filePath);
        removeNewFile(uploadFile);
        return new ImageDto(filePath.toString(), fileName);
    }

    // 로컬에 저장된 임시 파일 삭제
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("Temp file delete success");
        } else {
            log.info("Temp file delete fail");
        }
    }

    // MultipartFile을 로컬 파일로 변환
    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
