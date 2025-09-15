"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { trpc } from "@/lib/trpc"
import { Users, Settings, Calendar, MessageCircle, Search, Star, ArrowRight, BookOpen } from "lucide-react"
import Link from "next/link"

export default function DashboardPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: recommendedMentors } = trpc.mentor.getRecommendedMentors.useQuery(
    { interests: [], limit: 3 },
    { enabled: session?.user.role === "mentee" },
  )

  if (status === "loading") {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div>Loading...</div>
      </div>
    )
  }

  if (!session) {
    return null
  }

  const isMentor = session.user.role === "mentor"

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <Users className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-xl font-semibold text-foreground">MentorConnect</h1>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-muted-foreground">Welcome, {session.user.name}</span>
            <Link href="/profile">
              <Button variant="outline" size="sm">
                <Settings className="w-4 h-4 mr-2" />
                Profile
              </Button>
            </Link>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">
            {isMentor ? "Mentor Dashboard" : "Mentee Dashboard"}
          </h2>
          <p className="text-muted-foreground">
            {isMentor
              ? "Manage your mentoring sessions and help students grow"
              : "Explore career opportunities and connect with mentors"}
          </p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {isMentor ? (
            <>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Calendar className="w-5 h-5 mr-2 text-primary" />
                    My Sessions
                  </CardTitle>
                  <CardDescription>Manage your upcoming mentoring sessions</CardDescription>
                </CardHeader>
                <CardContent>
                  <Link href="/meetings">
                    <Button className="w-full">View Sessions</Button>
                  </Link>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <MessageCircle className="w-5 h-5 mr-2 text-primary" />
                    Messages
                  </CardTitle>
                  <CardDescription>Connect with your mentees</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button className="w-full bg-transparent" variant="outline">
                    View Messages
                  </Button>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Settings className="w-5 h-5 mr-2 text-primary" />
                    Availability
                  </CardTitle>
                  <CardDescription>Set your meeting availability and rates</CardDescription>
                </CardHeader>
                <CardContent>
                  <Link href="/meetings/create">
                    <Button className="w-full bg-transparent" variant="outline">
                      Create Session
                    </Button>
                  </Link>
                </CardContent>
              </Card>
            </>
          ) : (
            <>
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Search className="w-5 h-5 mr-2 text-primary" />
                    Find Mentors
                  </CardTitle>
                  <CardDescription>Browse and connect with professional mentors</CardDescription>
                </CardHeader>
                <CardContent>
                  <Link href="/mentors">
                    <Button className="w-full">Browse Mentors</Button>
                  </Link>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <Calendar className="w-5 h-5 mr-2 text-primary" />
                    My Meetings
                  </CardTitle>
                  <CardDescription>View your scheduled mentoring sessions</CardDescription>
                </CardHeader>
                <CardContent>
                  <Link href="/meetings">
                    <Button className="w-full bg-transparent" variant="outline">
                      View Meetings
                    </Button>
                  </Link>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center">
                    <MessageCircle className="w-5 h-5 mr-2 text-primary" />
                    Messages
                  </CardTitle>
                  <CardDescription>Chat with your mentors</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button className="w-full bg-transparent" variant="outline">
                    View Messages
                  </Button>
                </CardContent>
              </Card>
            </>
          )}
        </div>

        <div className="mt-12">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-2xl font-semibold text-foreground">Community Hub</h3>
              <p className="text-muted-foreground">Explore resources and connect with the community</p>
            </div>
            <Link href="/community">
              <Button variant="outline">
                View All
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            </Link>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Link href="/community/faq">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader className="pb-3">
                  <CardTitle className="flex items-center text-base">
                    <MessageCircle className="w-4 h-4 mr-2 text-primary" />
                    FAQ Guide
                  </CardTitle>
                  <CardDescription className="text-sm">Questions to ask mentors</CardDescription>
                </CardHeader>
              </Card>
            </Link>

            <Link href="/community/articles">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader className="pb-3">
                  <CardTitle className="flex items-center text-base">
                    <BookOpen className="w-4 h-4 mr-2 text-primary" />
                    Career Insights
                  </CardTitle>
                  <CardDescription className="text-sm">Professional articles</CardDescription>
                </CardHeader>
              </Card>
            </Link>

            <Link href="/community/guides">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader className="pb-3">
                  <CardTitle className="flex items-center text-base">
                    <Star className="w-4 h-4 mr-2 text-primary" />
                    Career Paths
                  </CardTitle>
                  <CardDescription className="text-sm">Explore career options</CardDescription>
                </CardHeader>
              </Card>
            </Link>

            <Link href="/mentors">
              <Card className="hover:shadow-lg transition-shadow cursor-pointer">
                <CardHeader className="pb-3">
                  <CardTitle className="flex items-center text-base">
                    <Users className="w-4 h-4 mr-2 text-primary" />
                    Find Mentors
                  </CardTitle>
                  <CardDescription className="text-sm">Connect with pros</CardDescription>
                </CardHeader>
              </Card>
            </Link>
          </div>
        </div>

        {!isMentor && recommendedMentors && recommendedMentors.length > 0 && (
          <div className="mt-12">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h3 className="text-2xl font-semibold text-foreground">Recommended Mentors</h3>
                <p className="text-muted-foreground">Mentors that match your interests and career goals</p>
              </div>
              <Link href="/mentors">
                <Button variant="outline">
                  View All
                  <ArrowRight className="w-4 h-4 ml-2" />
                </Button>
              </Link>
            </div>

            <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
              {recommendedMentors.map((mentor) => (
                <Card key={mentor.id} className="hover:shadow-lg transition-shadow">
                  <CardHeader>
                    <div className="flex items-start space-x-3">
                      <Avatar className="w-12 h-12">
                        <AvatarImage src={mentor.profileImage || "/placeholder.svg"} alt={mentor.name} />
                        <AvatarFallback>
                          {mentor.name
                            .split(" ")
                            .map((n) => n[0])
                            .join("")}
                        </AvatarFallback>
                      </Avatar>
                      <div className="flex-1 min-w-0">
                        <CardTitle className="text-base truncate">{mentor.name}</CardTitle>
                        <CardDescription className="text-sm">{mentor.jobTitle}</CardDescription>
                        <div className="flex items-center mt-1">
                          <Star className="w-3 h-3 mr-1 fill-yellow-400 text-yellow-400" />
                          <span className="text-xs text-muted-foreground">
                            {mentor.rating} ({mentor.reviewCount})
                          </span>
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-3">
                      <div className="flex flex-wrap gap-1">
                        {mentor.expertise.slice(0, 2).map((exp, index) => (
                          <Badge key={index} variant="secondary" className="text-xs">
                            {exp}
                          </Badge>
                        ))}
                        {mentor.expertise.length > 2 && (
                          <Badge variant="outline" className="text-xs">
                            +{mentor.expertise.length - 2}
                          </Badge>
                        )}
                      </div>
                      <Link href={`/mentors/${mentor.id}`}>
                        <Button size="sm" className="w-full">
                          View Profile
                        </Button>
                      </Link>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* Quick Actions */}
        <div className="mt-12">
          <h3 className="text-xl font-semibold text-foreground mb-4">Quick Actions</h3>
          <div className="flex flex-wrap gap-4">
            <Link href="/profile">
              <Button variant="outline">
                <Settings className="w-4 h-4 mr-2" />
                Complete Profile
              </Button>
            </Link>
            {isMentor ? (
              <Link href="/meetings/create">
                <Button variant="outline">
                  <Calendar className="w-4 h-4 mr-2" />
                  Create Session
                </Button>
              </Link>
            ) : (
              <Link href="/mentors">
                <Button variant="outline">
                  <Users className="w-4 h-4 mr-2" />
                  Explore Mentors
                </Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
