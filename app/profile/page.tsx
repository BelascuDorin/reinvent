"use client"

import type React from "react"

import { useSession } from "next-auth/react"
import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { Switch } from "@/components/ui/switch"
import { Users, ArrowLeft, Plus, X } from "lucide-react"
import Link from "next/link"

export default function ProfilePage() {
  const { data: session, status } = useSession()
  const router = useRouter()

  const [profile, setProfile] = useState({
    name: "",
    bio: "",
    jobTitle: "",
    company: "",
    expertise: [] as string[],
    interests: [] as string[],
    goals: "",
    meetingFee: 0,
    isFree: true,
    availability: "",
  })

  const [newExpertise, setNewExpertise] = useState("")
  const [newInterest, setNewInterest] = useState("")

  useEffect(() => {
    if (status === "loading") return
    if (!session) {
      router.push("/auth/signin")
      return
    }

    // Initialize profile with session data
    setProfile((prev) => ({
      ...prev,
      name: session.user.name || "",
    }))
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

  const isMentor = session.user.role === "mentor"

  const addExpertise = () => {
    if (newExpertise.trim() && !profile.expertise.includes(newExpertise.trim())) {
      setProfile((prev) => ({
        ...prev,
        expertise: [...prev.expertise, newExpertise.trim()],
      }))
      setNewExpertise("")
    }
  }

  const removeExpertise = (item: string) => {
    setProfile((prev) => ({
      ...prev,
      expertise: prev.expertise.filter((e) => e !== item),
    }))
  }

  const addInterest = () => {
    if (newInterest.trim() && !profile.interests.includes(newInterest.trim())) {
      setProfile((prev) => ({
        ...prev,
        interests: [...prev.interests, newInterest.trim()],
      }))
      setNewInterest("")
    }
  }

  const removeInterest = (item: string) => {
    setProfile((prev) => ({
      ...prev,
      interests: prev.interests.filter((i) => i !== item),
    }))
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    // TODO: Implement profile update with tRPC
    console.log("Profile updated:", profile)
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
              <h1 className="text-xl font-semibold text-foreground">Profile Settings</h1>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8 max-w-2xl">
        <div className="mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">{isMentor ? "Mentor Profile" : "Mentee Profile"}</h2>
          <p className="text-muted-foreground">
            {isMentor
              ? "Complete your profile to help mentees find and connect with you"
              : "Tell mentors about yourself and your career interests"}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>Your basic profile information</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="name">Full Name</Label>
                <Input
                  id="name"
                  value={profile.name}
                  onChange={(e) => setProfile((prev) => ({ ...prev, name: e.target.value }))}
                  placeholder="Your full name"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="bio">Bio</Label>
                <Textarea
                  id="bio"
                  value={profile.bio}
                  onChange={(e) => setProfile((prev) => ({ ...prev, bio: e.target.value }))}
                  placeholder={
                    isMentor
                      ? "Tell mentees about your background, experience, and what you can help with..."
                      : "Tell mentors about yourself, your background, and what you're looking for..."
                  }
                  rows={4}
                />
              </div>

              {isMentor && (
                <>
                  <div className="space-y-2">
                    <Label htmlFor="jobTitle">Job Title</Label>
                    <Input
                      id="jobTitle"
                      value={profile.jobTitle}
                      onChange={(e) => setProfile((prev) => ({ ...prev, jobTitle: e.target.value }))}
                      placeholder="e.g., Senior Software Engineer"
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="company">Company</Label>
                    <Input
                      id="company"
                      value={profile.company}
                      onChange={(e) => setProfile((prev) => ({ ...prev, company: e.target.value }))}
                      placeholder="e.g., Google, Microsoft, Local Startup"
                    />
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {/* Expertise/Interests */}
          <Card>
            <CardHeader>
              <CardTitle>{isMentor ? "Areas of Expertise" : "Interests & Goals"}</CardTitle>
              <CardDescription>
                {isMentor
                  ? "What topics can you help mentees with?"
                  : "What career areas are you interested in exploring?"}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {isMentor ? (
                <div className="space-y-2">
                  <Label>Expertise Areas</Label>
                  <div className="flex gap-2">
                    <Input
                      value={newExpertise}
                      onChange={(e) => setNewExpertise(e.target.value)}
                      placeholder="e.g., Software Development, Marketing"
                      onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addExpertise())}
                    />
                    <Button type="button" onClick={addExpertise} size="sm">
                      <Plus className="w-4 h-4" />
                    </Button>
                  </div>
                  <div className="flex flex-wrap gap-2 mt-2">
                    {profile.expertise.map((item, index) => (
                      <Badge key={index} variant="secondary" className="flex items-center gap-1">
                        {item}
                        <button
                          type="button"
                          onClick={() => removeExpertise(item)}
                          className="ml-1 hover:text-destructive"
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </Badge>
                    ))}
                  </div>
                </div>
              ) : (
                <>
                  <div className="space-y-2">
                    <Label>Career Interests</Label>
                    <div className="flex gap-2">
                      <Input
                        value={newInterest}
                        onChange={(e) => setNewInterest(e.target.value)}
                        placeholder="e.g., Technology, Medicine, Business"
                        onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addInterest())}
                      />
                      <Button type="button" onClick={addInterest} size="sm">
                        <Plus className="w-4 h-4" />
                      </Button>
                    </div>
                    <div className="flex flex-wrap gap-2 mt-2">
                      {profile.interests.map((item, index) => (
                        <Badge key={index} variant="secondary" className="flex items-center gap-1">
                          {item}
                          <button
                            type="button"
                            onClick={() => removeInterest(item)}
                            className="ml-1 hover:text-destructive"
                          >
                            <X className="w-3 h-3" />
                          </button>
                        </Badge>
                      ))}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="goals">Career Goals</Label>
                    <Textarea
                      id="goals"
                      value={profile.goals}
                      onChange={(e) => setProfile((prev) => ({ ...prev, goals: e.target.value }))}
                      placeholder="What are you hoping to achieve? What questions do you have about different careers?"
                      rows={3}
                    />
                  </div>
                </>
              )}
            </CardContent>
          </Card>

          {/* Mentor-specific settings */}
          {isMentor && (
            <Card>
              <CardHeader>
                <CardTitle>Meeting Settings</CardTitle>
                <CardDescription>Configure your availability and pricing</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label>Offer Free Sessions</Label>
                    <p className="text-sm text-muted-foreground">Provide free mentoring sessions to help students</p>
                  </div>
                  <Switch
                    checked={profile.isFree}
                    onCheckedChange={(checked) => setProfile((prev) => ({ ...prev, isFree: checked }))}
                  />
                </div>

                {!profile.isFree && (
                  <div className="space-y-2">
                    <Label htmlFor="meetingFee">Session Fee (RON)</Label>
                    <Input
                      id="meetingFee"
                      type="number"
                      value={profile.meetingFee}
                      onChange={(e) => setProfile((prev) => ({ ...prev, meetingFee: Number(e.target.value) }))}
                      placeholder="0"
                      min="0"
                    />
                  </div>
                )}

                <div className="space-y-2">
                  <Label htmlFor="availability">Availability Notes</Label>
                  <Textarea
                    id="availability"
                    value={profile.availability}
                    onChange={(e) => setProfile((prev) => ({ ...prev, availability: e.target.value }))}
                    placeholder="e.g., Weekday evenings, Weekend mornings, Flexible schedule"
                    rows={2}
                  />
                </div>
              </CardContent>
            </Card>
          )}

          <div className="flex gap-4">
            <Button type="submit" className="flex-1">
              Save Profile
            </Button>
            <Link href="/dashboard">
              <Button variant="outline">Cancel</Button>
            </Link>
          </div>
        </form>
      </div>
    </div>
  )
}
