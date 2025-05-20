import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  CardBody,
  CardHeader,
  Button,
} from '@nextui-org/react';
import { ArrowLeft } from 'lucide-react';
import { apiService, Post, Category, Tag, PostStatus } from '../services/apiService.ts';
import PostForm from '../components/PostForm.tsx';

const EditPostPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [post, setPost] = useState<Post | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        // Fetch categories and tags in parallel
        const [categoriesResponse, tagsResponse] = await Promise.all([
          apiService.getCategories(),
          apiService.getTags()
        ]);

        setCategories(categoriesResponse);

        // Handle different response formats for tags
        if (Array.isArray(tagsResponse)) {
          setTags(tagsResponse);
        } else if (tagsResponse && typeof tagsResponse === 'object') {
          // Check if it's a paginated response with content property
          if ('content' in tagsResponse && Array.isArray(tagsResponse.content)) {
            setTags(tagsResponse.content);
          } else {
            // If it's not in the expected format but still an object,
            // try to convert it to an array (fallback)
            const tagArray = Object.values(tagsResponse).filter(
                (item): item is Tag => typeof item === 'object' && item !== null && 'id' in item
            );
            setTags(tagArray);
            console.warn('Unexpected tags response format, attempted conversion:', tagsResponse);
          }
        } else {
          setTags([]);
          console.error('Invalid tags response format:', tagsResponse);
        }

        // If editing an existing post, fetch it
        if (id) {
          const postResponse = await apiService.getPost(id);
          setPost(postResponse);
        }

        setError(null);
      } catch (err) {
        console.error('Error fetching data:', err);
        setError('Failed to load necessary data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [id]);

  const handleSubmit = async (postData: {
    title: string;
    content: string;
    categoryId: string;
    tagIds: string[];
    status: PostStatus;
  }) => {
    try {
      setIsSubmitting(true);
      setError(null);

      if (id) {
        await apiService.updatePost(id, {
          ...postData,
          id
        });
      } else {
        await apiService.createPost(postData);
      }

      navigate('/');
    } catch (err) {
      console.error('Error saving post:', err);
      setError('Failed to save the post. Please try again.');
      setIsSubmitting(false);
    }
  };

  const handleCancel = () => {
    if (id) {
      navigate(`/posts/${id}`);
    } else {
      navigate('/');
    }
  };

  if (loading) {
    return (
        <div className="max-w-4xl mx-auto px-4">
          <Card className="w-full animate-pulse">
            <CardBody>
              <div className="h-8 bg-default-200 rounded w-3/4 mb-4"></div>
              <div className="space-y-3">
                <div className="h-4 bg-default-200 rounded w-full"></div>
                <div className="h-4 bg-default-200 rounded w-full"></div>
                <div className="h-4 bg-default-200 rounded w-2/3"></div>
              </div>
            </CardBody>
          </Card>
        </div>
    );
  }

  return (
      <div className="max-w-4xl mx-auto px-4">
        <Card className="w-full">
          <CardHeader className="flex justify-between items-center">
            <div className="flex items-center gap-4">
              <Button
                  variant="flat"
                  startContent={<ArrowLeft size={16} />}
                  onClick={handleCancel}
              >
                Back
              </Button>
              <h1 className="text-2xl font-bold">
                {id ? 'Edit Post' : 'Create New Post'}
              </h1>
            </div>
          </CardHeader>

          <CardBody>
            {error && (
                <div className="mb-4 p-4 text-red-500 bg-red-50 rounded-lg">
                  {error}
                </div>
            )}

            <PostForm
                initialPost={post}
                onSubmit={handleSubmit}
                onCancel={handleCancel}
                categories={categories}
                availableTags={tags}
                isSubmitting={isSubmitting}
            />
          </CardBody>
        </Card>
      </div>
  );
};

export default EditPostPage;