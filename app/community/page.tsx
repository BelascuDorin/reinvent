"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { trpc } from "@/lib/trpc"
import { Users, ArrowLeft, BookOpen, MessageSquare, Star, Clock, ArrowRight } from "lucide-react"
import Link from "next/link"

export default function CommunityPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: articles, isLoading } = trpc.community.getArticles.useQuery({ limit: 6 })

  if (status === "loading" || isLoading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div>Loading...</div>
      </div>
    )
  }

  if (!session) {
    return null
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <Link href="/dashboard">
              <Button variant="ghost" size="sm">
                <ArrowLeft className="w-4 h-4 mr-2" />
                Back to Dashboard
              </Button>
            </Link>
            <div className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <Users className="w-5 h-5 text-primary-foreground" />
              </div>
              <h1 className="text-xl font-semibold text-foreground">Community</h1>
            </div>
          </div>
          {session.user.role === "mentor" && (
            <Link href="/community/write">
              <Button>Write Article</Button>
            </Link>
          )}
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Community Hub</h2>
          <p className="text-muted-foreground">
            Discover insights, ask questions, and learn from the Romanian professional community
          </p>
        </div>

        {/* Quick Links */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4 mb-12">
          <Link href="/community/faq">
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center text-lg">
                  <MessageSquare className="w-5 h-5 mr-2 text-primary" />
                  FAQ Guide
                </CardTitle>
                <CardDescription>Common questions to ask mentors</CardDescription>
              </CardHeader>
            </Card>
          </Link>

          <Link href="/community/articles">
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center text-lg">
                  <BookOpen className="w-5 h-5 mr-2 text-primary" />
                  Career Insights
                </CardTitle>
                <CardDescription>Articles from industry professionals</CardDescription>
              </CardHeader>
            </Card>
          </Link>

          <Link href="/community/guides">
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center text-lg">
                  <Star className="w-5 h-5 mr-2 text-primary" />
                  Career Paths
                </CardTitle>
                <CardDescription>Explore different career journeys</CardDescription>
              </CardHeader>
            </Card>
          </Link>

          <Link href="/mentors">
            <Card className="hover:shadow-lg transition-shadow cursor-pointer">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center text-lg">
                  <Users className="w-5 h-5 mr-2 text-primary" />
                  Find Mentors
                </CardTitle>
                <CardDescription>Connect with professionals</CardDescription>
              </CardHeader>
            </Card>
          </Link>
        </div>

        {/* Latest Articles */}
        <div className="mb-12">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-2xl font-semibold text-foreground">Latest Insights</h3>
              <p className="text-muted-foreground">Recent articles from our mentor community</p>
            </div>
            <Link href="/community/articles">
              <Button variant="outline">
                View All
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </Link>
          </div>

          {!articles || articles.length === 0 ? (
            <Card>
              <CardContent className="py-12 text-center">
                <BookOpen className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-semibold text-foreground mb-2">No Articles Yet</h3>
                <p className="text-muted-foreground">
                  Be the first to share your professional insights with the community
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {articles.map((article) => (
                <Card key={article.id} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex items-center space-x-2 mb-2">
                      <Badge variant="secondary">{article.category}</Badge>
                      <div className="flex items-center text-xs text-muted-foreground">
                        <Clock className="w-3 h-3 mr-1" />
                        {article.readTime} min read
                      </div>
                    </div>
                    <CardTitle className="text-lg line-clamp-2">{article.title}</CardTitle>
                    <CardDescription className="line-clamp-3">{article.excerpt}</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center space-x-2">
                        <Avatar className="w-6 h-6">
                          <AvatarFallback className="text-xs">
                            {article.authorName
                              .split(" ")
                              .map((n) => n[0])
                              .join("")}
                          </AvatarFallback>
                        </Avatar>
                        <span className="text-sm text-muted-foreground">{article.authorName}</span>
                      </div>
                      <Link href={`/community/articles/${article.id}`}>
                        <Button size="sm" variant="outline">
                          Read
                        </Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>

        {/* Community Stats */}
        <Card>
          <CardHeader>
            <CardTitle>Community Impact</CardTitle>
            <CardDescription>See how our community is helping Romanian students</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-6 text-center">
              <div>
                <div className="text-3xl font-bold text-primary mb-2">150+</div>
                <p className="text-sm text-muted-foreground">Active Mentors</p>
              </div>
              <div>
                <div className="text-3xl font-bold text-primary mb-2">500+</div>
                <p className="text-sm text-muted-foreground">Students Helped</p>
              </div>
              <div>
                <div className="text-3xl font-bold text-primary mb-2">1,200+</div>
                <p className="text-sm text-muted-foreground">Sessions Completed</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
