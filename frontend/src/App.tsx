import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import NavBar from "./components/NavBar.tsx";
import HomePage from "./pages/HomePage.tsx";
import EditPostPage from "./pages/EditPostPage.tsx";
import PostPage from "./pages/PostPage.tsx";
import CategoriesPage from "./pages/CategoriesPage.tsx";
import TagsPage from "./pages/TagsPage.tsx";
import DraftsPage from "./pages/DraftsPage.tsx";
import LoginPage from "./pages/LoginPage.tsx";
import { AuthProvider, useAuth } from "./components/AuthContext.tsx";

// Protected Route component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

function AppContent() {
  const { isAuthenticated, logout, user } = useAuth();

  return (
    <BrowserRouter>
      <NavBar 
        isAuthenticated={isAuthenticated}
        userProfile={user ? {
          name: user.name,
          avatar: undefined // Add avatar support if needed
        } : undefined}
        onLogout={logout}
      />
      <main className="container mx-auto py-6">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route 
            path="/posts/new" 
            element={
              <ProtectedRoute>
                <EditPostPage />
              </ProtectedRoute>
            } 
          />
          <Route path="/posts/:id" element={<PostPage isAuthenticated={isAuthenticated}/>} />
          <Route 
            path="/posts/:id/edit" 
            element={
              <ProtectedRoute>
                <EditPostPage />
              </ProtectedRoute>
            } 
          />
          <Route path="/categories" element={<CategoriesPage isAuthenticated={isAuthenticated}/>} />
          <Route path="/tags" element={<TagsPage isAuthenticated={isAuthenticated}/>} />
          <Route 
            path="/posts/drafts" 
            element={
              <ProtectedRoute>
                <DraftsPage />
              </ProtectedRoute>
            } 
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;