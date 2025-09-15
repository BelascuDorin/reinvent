"use client"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Users, ArrowLeft, Star, TrendingUp, Briefcase, GraduationCap } from "lucide-react"
import Link from "next/link"

export default function GuidesPage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

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

  const careerPaths = [
    {
      id: "tech",
      title: "Technology & Software",
      description: "Explore careers in software development, data science, and tech innovation",
      icon: <TrendingUp className="w-6 h-6" />,
      roles: ["Software Engineer", "Data Scientist", "Product Manager", "UX Designer"],
      growth: "High",
      avgSalary: "8,000 - 15,000 RON",
      education: "Computer Science, Engineering, or related field",
      skills: ["Programming", "Problem Solving", "Analytical Thinking"],
    },
    {
      id: "business",
      title: "Business & Management",
      description: "Leadership roles in various industries and business functions",
      icon: <Briefcase className="w-6 h-6" />,
      roles: ["Business Analyst", "Project Manager", "Consultant", "Operations Manager"],
      growth: "Steady",
      avgSalary: "5,000 - 12,000 RON",
      education: "Business, Economics, or related field",
      skills: ["Leadership", "Communication", "Strategic Thinking"],
    },
    {
      id: "marketing",
      title: "Marketing & Communications",
      description: "Creative and analytical roles in marketing and brand management",
      icon: <Star className="w-6 h-6" />,
      roles: ["Digital Marketer", "Brand Manager", "Content Creator", "PR Specialist"],
      growth: "High",
      avgSalary: "4,000 - 10,000 RON",
      education: "Marketing, Communications, or related field",
      skills: ["Creativity", "Analytics", "Communication"],
    },
    {
      id: "healthcare",
      title: "Healthcare & Medicine",
      description: "Careers focused on health, wellness, and medical care",
      icon: <GraduationCap className="w-6 h-6" />,
      roles: ["Doctor", "Nurse", "Pharmacist", "Medical Researcher"],
      growth: "Steady",
      avgSalary: "6,000 - 20,000 RON",
      education: "Medical degree and specialized training",
      skills: ["Empathy", "Attention to Detail", "Scientific Knowledge"],
    },
  ]

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center space-x-4">
          <Link href="/community">
            <Button variant="ghost" size="sm">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back to Community
            </Button>
          </Link>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <Star className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-xl font-semibold text-foreground">Career Path Guides</h1>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">Explore Career Paths</h2>
          <p className="text-muted-foreground">
            Discover different career opportunities and what it takes to succeed in each field
          </p>
        </div>

        {/* Career Path Cards */}
        <div className="grid md:grid-cols-2 gap-8 mb-12">
          {careerPaths.map((path) => (
            <Card key={path.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="flex items-start space-x-3">
                  <div className="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center text-primary">
                    {path.icon}
                  </div>
                  <div className="flex-1">
                    <CardTitle className="text-xl">{path.title}</CardTitle>
                    <CardDescription className="mt-1">{path.description}</CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* Common Roles */}
                  <div>
                    <h4 className="font-semibold text-sm text-foreground mb-2">Common Roles</h4>
                    <div className="flex flex-wrap gap-1">
                      {path.roles.map((role, index) => (
                        <Badge key={index} variant="secondary" className="text-xs">
                          {role}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  {/* Key Info */}
                  <div className="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <p className="font-medium text-foreground">Job Growth</p>
                      <p className="text-muted-foreground">{path.growth}</p>
                    </div>
                    <div>
                      <p className="font-medium text-foreground">Avg. Salary</p>
                      <p className="text-muted-foreground">{path.avgSalary}</p>
                    </div>
                  </div>

                  {/* Education */}
                  <div>
                    <h4 className="font-semibold text-sm text-foreground mb-1">Typical Education</h4>
                    <p className="text-sm text-muted-foreground">{path.education}</p>
                  </div>

                  {/* Key Skills */}
                  <div>
                    <h4 className="font-semibold text-sm text-foreground mb-2">Key Skills</h4>
                    <div className="flex flex-wrap gap-1">
                      {path.skills.map((skill, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {skill}
                        </Badge>
                      ))}
                    </div>
                  </div>

                  <div className="pt-2">
                    <Link href={`/mentors?industry=${encodeURIComponent(path.title)}`}>
                      <Button className="w-full">Find Mentors in This Field</Button>
                    </Link>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Next Steps */}
        <Card>
          <CardHeader>
            <CardTitle>Ready to Explore?</CardTitle>
            <CardDescription>Take the next step in your career journey</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid md:grid-cols-3 gap-4">
              <Link href="/mentors">
                <Button
                  variant="outline"
                  className="w-full h-auto p-4 flex flex-col items-center space-y-2 bg-transparent"
                >
                  <Users className="w-6 h-6" />
                  <span>Find Mentors</span>
                  <span className="text-xs text-muted-foreground">Connect with professionals</span>
                </Button>
              </Link>

              <Link href="/community/faq">
                <Button
                  variant="outline"
                  className="w-full h-auto p-4 flex flex-col items-center space-y-2 bg-transparent"
                >
                  <Star className="w-6 h-6" />
                  <span>Prepare Questions</span>
                  <span className="text-xs text-muted-foreground">Get ready for sessions</span>
                </Button>
              </Link>

              <Link href="/browse">
                <Button
                  variant="outline"
                  className="w-full h-auto p-4 flex flex-col items-center space-y-2 bg-transparent"
                >
                  <TrendingUp className="w-6 h-6" />
                  <span>Book Sessions</span>
                  <span className="text-xs text-muted-foreground">Start learning today</span>
                </Button>
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
