"use client"

import { useSession } from "next-auth/react"
import { useRouter, useParams } from "next/navigation"
import { useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Separator } from "@/components/ui/separator"
import { trpc } from "@/lib/trpc"
import { Users, ArrowLeft, Star, MapPin, Briefcase, Calendar, MessageCircle, Languages, Clock } from "lucide-react"
import Link from "next/link"

export default function MentorProfilePage() {
  const { data: session, status } = useSession()
  const router = useRouter()
  const params = useParams()
  const mentorId = params.id as string

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }
  }, [session, status, router])

  const { data: mentor, isLoading } = trpc.mentor.getMentorById.useQuery({ id: mentorId })
  const { data: availableSlots } = trpc.meeting.getAvailableSlots.useQuery({ mentorId })

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

  if (!mentor) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Card>
          <CardContent className="py-8 text-center">
            <h3 className="text-lg font-semibold text-foreground mb-2">Mentor Not Found</h3>
            <p className="text-muted-foreground mb-4">
              The mentor you're looking for doesn't exist or has been removed.
            </p>
            <Link href="/mentors">
              <Button>Browse Other Mentors</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center space-x-4">
          <Link href="/mentors">
            <Button variant="ghost" size="sm">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back to Mentors
            </Button>
          </Link>
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <Users className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-xl font-semibold text-foreground">Mentor Profile</h1>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8 max-w-4xl">
        {/* Profile Header */}
        <Card className="mb-8">
          <CardContent className="pt-6">
            <div className="flex flex-col md:flex-row gap-6">
              <Avatar className="w-32 h-32 mx-auto md:mx-0">
                <AvatarImage src={mentor.profileImage || "/placeholder.svg"} alt={mentor.name} />
                <AvatarFallback className="text-2xl">
                  {mentor.name
                    .split(" ")
                    .map((n) => n[0])
                    .join("")}
                </AvatarFallback>
              </Avatar>

              <div className="flex-1 text-center md:text-left">
                <h1 className="text-3xl font-bold text-foreground mb-2">{mentor.name}</h1>
                <p className="text-xl text-muted-foreground mb-4">
                  {mentor.jobTitle} at {mentor.company}
                </p>

                <div className="flex flex-wrap justify-center md:justify-start gap-4 mb-4">
                  <div className="flex items-center text-sm">
                    <Star className="w-4 h-4 mr-1 fill-yellow-400 text-yellow-400" />
                    {mentor.rating} ({mentor.reviewCount} reviews)
                  </div>
                  <div className="flex items-center text-sm text-muted-foreground">
                    <Briefcase className="w-4 h-4 mr-1" />
                    {mentor.experience} years experience
                  </div>
                  <div className="flex items-center text-sm text-muted-foreground">
                    <MapPin className="w-4 h-4 mr-1" />
                    {mentor.location}
                  </div>
                </div>

                <div className="flex flex-wrap justify-center md:justify-start gap-2">
                  {mentor.expertise.map((exp, index) => (
                    <Badge key={index} variant="secondary">
                      {exp}
                    </Badge>
                  ))}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="grid lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* About */}
            <Card>
              <CardHeader>
                <CardTitle>About {mentor.name.split(" ")[0]}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground leading-relaxed">{mentor.bio}</p>
              </CardContent>
            </Card>

            {/* Expertise & Skills */}
            <Card>
              <CardHeader>
                <CardTitle>Areas of Expertise</CardTitle>
                <CardDescription>What {mentor.name.split(" ")[0]} can help you with</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid md:grid-cols-2 gap-3">
                  {mentor.expertise.map((exp, index) => (
                    <div key={index} className="flex items-center p-3 bg-muted rounded-lg">
                      <div className="w-2 h-2 bg-primary rounded-full mr-3" />
                      <span className="font-medium">{exp}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* Meeting Types */}
            <Card>
              <CardHeader>
                <CardTitle>Session Types</CardTitle>
                <CardDescription>Types of mentoring sessions offered</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {mentor.meetingTypes.map((type, index) => (
                    <div key={index} className="flex items-center p-3 border rounded-lg">
                      <MessageCircle className="w-5 h-5 text-primary mr-3" />
                      <span>{type}</span>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Quick Info */}
            <Card>
              <CardHeader>
                <CardTitle>Quick Info</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center">
                  <Languages className="w-4 h-4 mr-3 text-muted-foreground" />
                  <div>
                    <p className="font-medium">Languages</p>
                    <p className="text-sm text-muted-foreground">{mentor.languages.join(", ")}</p>
                  </div>
                </div>

                <Separator />

                <div className="flex items-center">
                  <Clock className="w-4 h-4 mr-3 text-muted-foreground" />
                  <div>
                    <p className="font-medium">Availability</p>
                    <p className="text-sm text-muted-foreground">{mentor.availability}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Available Sessions */}
            <Card>
              <CardHeader>
                <CardTitle>Available Sessions</CardTitle>
                <CardDescription>Book a session with {mentor.name.split(" ")[0]}</CardDescription>
              </CardHeader>
              <CardContent>
                {!availableSlots || availableSlots.length === 0 ? (
                  <div className="text-center py-4">
                    <Calendar className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                    <p className="text-sm text-muted-foreground">No sessions available at the moment</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {availableSlots.slice(0, 3).map((slot) => (
                      <div key={slot.id} className="p-3 border rounded-lg">
                        <p className="font-medium text-sm">{slot.title}</p>
                        <p className="text-xs text-muted-foreground">
                          {new Date(slot.date).toLocaleDateString()} • {slot.startTime}
                        </p>
                        <p className="text-xs font-medium text-primary">
                          {slot.price === 0 ? "Free" : `${slot.price} RON`}
                        </p>
                      </div>
                    ))}
                    {availableSlots.length > 3 && (
                      <p className="text-xs text-muted-foreground text-center">
                        +{availableSlots.length - 3} more sessions
                      </p>
                    )}
                  </div>
                )}

                <div className="mt-4 space-y-2">
                  <Link href="/browse">
                    <Button className="w-full">Book a Session</Button>
                  </Link>
                  <Button variant="outline" className="w-full bg-transparent">
                    <MessageCircle className="w-4 h-4 mr-2" />
                    Send Message
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
