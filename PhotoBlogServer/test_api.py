import unittest
import urllib.request
import urllib.error
import json

class PhotoBlogAPITest(unittest.TestCase):
    """
    강의자료(2) 기반 포토 블로그 API 검증 엔진 (Pure Python unittest)
    실행 중인 Django 서버의 API 응답과 JSON 데이터 구조를 자동으로 검증합니다.
    """
    
    BASE_URL = "http://127.0.0.1:8000"
    API_URL = f"{BASE_URL}/api_root/Post/"

    def test_01_server_connection(self):
        """1. 서버 접속 및 루트 페이지(200 OK) 응답 검증"""
        req = urllib.request.Request(self.BASE_URL)
        with urllib.request.urlopen(req, timeout=5) as response:
            self.assertEqual(response.status, 200, "서버 루트 페이지가 정상 응답(200)을 반환해야 합니다.")

    def test_02_api_get_status_code(self):
        """2. GET /api_root/Post/ API 응답 상태 코드(200 OK) 검증"""
        req = urllib.request.Request(self.API_URL)
        with urllib.request.urlopen(req, timeout=5) as response:
            self.assertEqual(response.status, 200, "API 엔드포인트가 200 OK를 반환해야 합니다.")

    def test_03_api_content_type_json(self):
        """3. API 응답 헤더의 Content-Type이 application/json인지 검증"""
        req = urllib.request.Request(self.API_URL)
        with urllib.request.urlopen(req, timeout=5) as response:
            content_type = response.headers.get('Content-Type', '')
            self.assertIn('application/json', content_type, "응답 Content-Type에 application/json이 포함되어야 합니다.")

    def test_04_api_json_structure(self):
        """4. API 반환 JSON 파싱 및 필수 필드(title, text, image) 검증"""
        req = urllib.request.Request(self.API_URL)
        with urllib.request.urlopen(req, timeout=5) as response:
            raw_data = response.read().decode('utf-8')
            json_data = json.loads(raw_data)

            # JSON 응답이 리스트 형태인지 확인
            self.assertIsInstance(json_data, list, "API 응답은 리스트 형태여야 합니다.")
            self.assertGreater(len(json_data), 0, "최소 1개 이상의 게시물이 등록되어 있어야 합니다.")

            # 첫 번째 게시글의 필수 키 검증
            first_post = json_data[0]
            required_fields = ['title', 'text', 'image']
            for field in required_fields:
                self.assertIn(field, first_post, f"게시글 JSON에 '{field}' 필드가 존재해야 합니다.")

    def test_05_api_post_detail(self):
        """5. 첫 번째 게시글의 단일 상세 조회(GET /api_root/Post/1/) 검증"""
        detail_url = f"{self.API_URL}1/"
        try:
            req = urllib.request.Request(detail_url)
            with urllib.request.urlopen(req, timeout=5) as response:
                self.assertEqual(response.status, 200, "개별 게시글 상세 API 응답은 200이어야 합니다.")
                post_data = json.loads(response.read().decode('utf-8'))
                self.assertIn('title', post_data)
        except urllib.error.HTTPError as e:
            # 1번 게시물이 없는 경우 스킵하거나 상태 확인
            if e.code == 404:
                self.skipTest("1번 게시물이 없어 단일 조회를 건너뜁니다.")
            else:
                raise e

if __name__ == '__main__':
    # unittest 테스트 러너 구동
    unittest.main(verbosity=2)
