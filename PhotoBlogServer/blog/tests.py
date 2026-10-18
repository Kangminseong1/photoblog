from django.test import TestCase
from django.contrib.auth.models import User
from blog.models import Post

class BlogTests(TestCase):
    def setUp(self):
        self.user = User.objects.create_user(username='tmp', password='1')
        self.post = Post.objects.create(author=self.user, title='title', text='text')

    def test_str(self):
        self.assertEqual(str(self.post), 'title')

    def test_list(self):
        res = self.client.get('')
        self.assertEqual(res.status_code, 200)
        
    def test_detail(self):
        res = self.client.get(f'/post/{self.post.pk}/')
        self.assertEqual(res.status_code, 200)

    def test_new(self):
        self.client.login(username='tmp', password='1')
        res = self.client.post('/post/new/', {'title':'new','text':'new'})
        self.assertEqual(res.status_code, 302)
        self.assertTrue(Post.objects.filter(title='new').exists())

    def test_edit(self):
        self.client.login(username='tmp', password='1')
        res = self.client.post(f'/post/{self.post.pk}/edit/', {'title': 'edit', 'text':'edit'})
        self.assertEqual(res.status_code, 302)
        self.assertTrue(Post.objects.filter(title='edit').exists())

    def test_publish(self):
        self.assertIsNone(self.post.published_date)
        self.post.publish()
        self.post.refresh_from_db()
        self.assertIsNotNone(self.post.published_date)

    def test_new_get(self):
        self.client.login(username='tmp', password='1')
        res = self.client.get('/post/new/')
        self.assertEqual(res.status_code, 200)

    def test_edit_get(self):
        self.client.login(username='tmp', password='1')
        res = self.client.get(f'/post/{self.post.pk}/edit/')
        self.assertEqual(res.status_code, 200)

    def test_api_root(self):
        self.assertEqual(self.client.get('/api_root/').status_code, 200)

    def test_api_list(self):
        self.assertEqual(self.client.get('/api_root/Post/').status_code, 200)

    def test_api_create(self):
        res = self.client.post('/api_root/Post/', {'author': self.user.pk, 'title': 'a', 'text': 'b'})
        self.assertEqual(res.status_code, 201)
        self.assertTrue(Post.objects.filter(title='a').exists())
